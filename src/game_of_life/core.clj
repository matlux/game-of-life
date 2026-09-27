(ns game-of-life.core
  (:gen-class))

(defn -main [& _]
  ;; Loading the engine or running tests must never open a desktop window.
  (require 'game-of-life.desktop)
  ((resolve 'game-of-life.desktop/launch!)))
