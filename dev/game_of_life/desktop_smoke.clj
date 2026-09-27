(ns game-of-life.desktop-smoke
  (:require [game-of-life.desktop :as desktop]))

(defn -main [& _]
  (let [drawn (promise)
        sketch (desktop/launch!
                {:features []
                 :draw (fn [state]
                         (desktop/draw state)
                         (when (>= (:generation state) 5) (deliver drawn state)))})]
    (try
      (let [state (deref drawn 15000 nil)]
        (when-not state (throw (ex-info "Desktop did not render five generations in 15 seconds." {})))
        (println "Desktop smoke passed: drew generation" (:generation state)))
      (finally (.exit sketch))))
  (shutdown-agents))
