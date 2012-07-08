(ns corrosive.core
  (:import [org.lwjgl.opengl Display DisplayMode GL11]
           [org.lwjgl.input Mouse Keyboard])
  (:use corrosive.infix)
  (:require [corrosive.timer :as timer]))

(def ^:dynamic ^Long *x* (atom 400))
(def ^:dynamic ^Long *y* (atom 300))
(def ^:dynamic *rotation* (atom 0))
           
(defmacro while-not [expr & body]
  `(while (not ~expr)
     ~@body))

(defmacro with-thread
  "Executed block in separate thread."
  [& body]
  `(.start
     (new Thread
          (fn []
            ~@body ))))
                
(defmacro gl-begin
  "Call body inside GL begin/end context."
  [type & body]
  `(do
     (GL11/glBegin ~type)
     ~@body
     (GL11/glEnd)))

(defmacro gl-with-current-matrix
  "Do computation with current space matrix. Restores it after computation."
  [& body]
  `(do
     (GL11/glPushMatrix)
     ~@body
     (GL11/glPopMatrix)))

(defn- init-gl
  "Initialize OpenGL with given w/h as resolution."
  [w h]
  (GL11/glMatrixMode GL11/GL_PROJECTION)
  (GL11/glLoadIdentity)
  (GL11/glOrtho 0 w 0 h 1 -1)
  (GL11/glMatrixMode GL11/GL_MODELVIEW))

(defn- render-gl
  "Game renderer."
  []
  (GL11/glClear (bit-or
                  GL11/GL_COLOR_BUFFER_BIT
                  GL11/GL_DEPTH_BUFFER_BIT))

  (GL11/glColor3f 0.5 0.5 0.5)

  ;; draw quad
  (gl-with-current-matrix
    (GL11/glTranslatef @*x* @*y* 0)
    (GL11/glRotatef @*rotation* 0.0 0.0 0.0)
    (GL11/glTranslatef (- @*x*)
                       (- @*y*)
                       0)

    (gl-begin GL11/GL_QUADS
      (GL11/glVertex2f (- @*x* 50) (- @*y* 50))
      (GL11/glVertex2f (+ @*x* 50) (- @*y* 50))
      (GL11/glVertex2f (+ @*x* 50) (+ @*y* 50))
      (GL11/glVertex2f (- @*x* 50) (+ @*y* 50))
) ) )

(defn- game-update
  "Update game using current delta time."
  [timer delta]
  (swap! *rotation* #($ % + delta * 3.5))

  (if (Keyboard/isKeyDown Keyboard/KEY_LEFT)
    (swap! *x* #($ % - 0.35 * delta)))

  (if (Keyboard/isKeyDown Keyboard/KEY_RIGHT)
    (swap! *x* #($ % + 0.35 * delta)))

  (if (Keyboard/isKeyDown Keyboard/KEY_UP)
    (swap! *y* #($ % + 0.35 * delta)))
  
  (if (Keyboard/isKeyDown Keyboard/KEY_DOWN)
    (swap! *y* #($ % - 0.35 * delta)))

  ;; keep it on screen
  (if (< @*x* 0)   (reset! *x* 0))
  (if (> @*x* 800) (reset! *x* 800))
  (if (< @*y* 0)   (reset! *y* 0))
  (if (> @*y* 600) (reset! *y* 600))

  (timer/update-fps! timer)
)

(defn game-main
  "Main entry point for game. Can be run inside future."
  []
  (let [w  800
        h  600
        tm (timer/create)]

    (Display/setDisplayMode (DisplayMode. w h))
    (Display/create)

    ;; OpenGL initialization
    (init-gl w h)

    (while-not (Display/isCloseRequested)
      (game-update tm (timer/get-delta tm))
      (render-gl)

      (Display/update)
      (Display/sync 60))
    (Display/destroy)
) )

;; eval this line to startup game
(def *game-thread* (-> (game-main) future))
