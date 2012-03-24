(ns corrosive.timer
  "Timing related code."
  (:import [org.lwjgl Sys]))

(defprotocol ITimer
  (set-fps        [this f])
  (set-last-frame [this v])
  (set-last-fps   [this v])
  (get-fps        [this])
  (get-last-frame [this])
  (get-last-fps   [this]))

(deftype Timer [^:volatile-mutable fps
                ^:volatile-mutable lastfps
                ^:volatile-mutable lastframe]
  ITimer
  (set-fps        [this f] (set! fps f))
  (set-last-frame [this v] (set! lastframe v))
  (set-last-fps   [this v] (set! lastfps v))
  (get-fps        [this] fps)
  (get-last-frame [this] lastframe)
  (get-last-fps   [this] lastfps))

(defn get-time
  "Return accurate current time in milliseconds."
  ^Long []
  (long
    (/
      (* 1000 (Sys/getTime))
      (Sys/getTimerResolution) )))

(defn create
  "Initialize Timer."
  []
  (Timer. 0 0 0))

(defn get-delta
  "Calculate how many milliseconds have passed since last frame. Updates timer."
  [^Timer timer]
  (let [time      (get-time)
        lastframe (get-last-frame timer)
        delta     (int (- time lastframe))]

    (set-last-frame timer time)
    delta))

(defn update-fps!
  "Updates FPS in timer. Get value with 'get-fps'."
  [^Timer timer]
  (let [lastfps (get-last-fps timer)
        fps     (get-fps timer)
        time    (get-time)
        diff    (- lastfps time)]
    (if (> diff 1000)
      (do
        (set-fps timer 0)
        (set-last-fps timer (+ lastfps 1000)))
      (set-fps timer (inc fps))
) ) )
