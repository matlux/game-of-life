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

(deftest edit-desktop-cells
  (doseq [paused? [false true]]
    (let [initial (assoc (desktop/initial-state) :paused? paused?)
          edited (desktop/mouse-pressed initial {:x 5 :y 5 :button :left})]
      (is (true? (get-in edited [:board 0])))
      (is (= paused? (:paused? edited)))
      (is (= 0 (:generation edited)))
      (is (= initial (desktop/mouse-pressed edited {:x 5 :y 5 :button :left})))
      (is (= initial (desktop/mouse-pressed initial {:x -1 :y 5 :button :left})))
      (is (= initial (desktop/mouse-pressed initial {:x 600 :y 5 :button :left}))))))
