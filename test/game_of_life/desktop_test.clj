(ns game-of-life.desktop-test
  (:require [clojure.test :refer [deftest is]]
            [game-of-life.desktop :as desktop]))

(deftest desktop-lifecycle
  (let [initial (desktop/initial-state)
        playing (desktop/update-state initial)
        paused (desktop/key-pressed playing {:key :space})
        stepped (desktop/key-pressed paused {:key :n})]
    (is (= 1 (:generation playing)))
    (is (= paused (desktop/update-state paused)))
    (is (= 2 (:generation stepped)))
    (is (:paused? stepped))
    (is (= initial (desktop/key-pressed stepped {:key :r})))
    (is (false? (:paused? (desktop/key-pressed paused {:key :space}))))))
