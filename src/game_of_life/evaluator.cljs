(ns game-of-life.evaluator
  (:require [cljs.js :as compiler]))

(defn evaluate-rule!
  "Compile actual ClojureScript and validate the cell-rule contract before installing it."
  [source callback]
  (compiler/eval-str
   (compiler/empty-state) source "rule.cljs"
   {:eval compiler/js-eval :context :expr :ns 'life.user
    ;; The playground supplies cljs.core. It never fetches arbitrary libraries.
    :load (fn [_ done] (done nil))}
   (fn [{:keys [error value]}]
     (if error
       (callback {:error (or (.-message (ex-cause error)) (.-message error) (str error))})
       (try
         (when-not (fn? value)
           (throw (js/Error. "The last expression must be a function of [alive? neighbours].")))
         (doseq [alive? [false true] neighbours (range 9)]
           (let [result (value alive? neighbours)]
             (when-not (or (true? result) (false? result))
               (throw (js/Error. "The rule must return true or false for every cell.")))))
         (callback {:rule value})
         (catch :default e
           (callback {:error (or (.-message e) (str e))})))))))
