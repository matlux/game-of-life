(ns game-of-life.worker
  (:require [game-of-life.engine :as engine]
            [game-of-life.evaluator :as evaluator]
            [game-of-life.patterns :as patterns]
            [game-of-life.rules :as rules]))

(defonce active-rule (atom rules/conway))

(defn reply! [id result]
  (.postMessage js/self (clj->js (assoc result :id id))))

(set! (.-onmessage js/self)
      (fn [event]
        (let [{:keys [id operation source state pattern boundary]}
              (js->clj (.-data event) :keywordize-keys true)]
          (try
            (case operation
              "seed" (if-let [seed (get patterns/all (keyword pattern))]
                       (reply! id {:state (engine/new-state seed (keyword boundary))})
                       (throw (js/Error. "Unknown pattern.")))
              "step" (reply! id {:state (engine/step (update state :boundary keyword) @active-rule)})
              "evaluate" (evaluator/evaluate-rule!
                          source
                          (fn [{:keys [error rule]}]
                            (if error
                              (reply! id {:error error})
                              (do (reset! active-rule rule) (reply! id {:applied true})))))
              (throw (js/Error. "Unknown operation.")))
            (catch :default e
              (reply! id {:error (or (.-message e) (str e))}))))))

(.postMessage js/self #js {:ready true})
