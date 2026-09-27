(ns game-of-life.test-runner
  (:require [cljs.test :as test]
            [game-of-life.engine-test]))

(defmethod test/report [::test/default :end-run-tests] [result]
  (when-not (test/successful? result)
    (set! (.-exitCode js/process) 1)))

(defn -main []
  (test/run-tests 'game-of-life.engine-test))

(set! *main-cli-fn* -main)
