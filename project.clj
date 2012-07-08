(defproject corrosive "0.1.0"
  :description "2D space shooter"
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [org.lwjgl.lwjgl/lwjgl "2.8.4"]]
  :warn-on-reflection true
  :jvm-opts ["-Djava.library.path=native/linux"])
