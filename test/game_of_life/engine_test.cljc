(ns game-of-life.engine-test
  (:require [game-of-life.engine :as engine]
            [game-of-life.patterns :as patterns]
            [game-of-life.rules :as rules]
            #?(:clj [clojure.test :refer [deftest is testing]]
               :cljs [cljs.test :refer-macros [deftest is testing]])))

(defn board [width height cells & [boundary]]
  (engine/new-state {:width width :height height :cells cells} (or boundary :dead)))

(defn live-cells [{:keys [width board]}]
  (into #{} (keep-indexed (fn [i alive?] (when alive? [(mod i width) (quot i width)])) board)))

(deftest conway-truth-table
  (doseq [alive? [false true] neighbours (range 9)]
    (is (= (contains? (if alive? #{2 3} #{3}) neighbours)
           (rules/conway alive? neighbours)))))

(deftest familiar-patterns
  (testing "A block stays still, a blinker oscillates, and a glider travels"
    (let [block (board 8 6 [[2 2] [3 2] [2 3] [3 3]])
          blinker (board 8 6 [[2 2] [3 2] [4 2]])
          glider (board 8 8 [[2 1] [3 2] [1 3] [2 3] [3 3]])]
      (is (= (:board block) (:board (engine/step block))))
      (is (= #{[3 1] [3 2] [3 3]} (live-cells (engine/step blinker))))
      (is (= (:board blinker) (:board (engine/step (engine/step blinker)))))
      (is (= #{[3 2] [4 3] [2 4] [3 4] [4 4]}
             (live-cells (nth (iterate engine/step glider) 4))))))
  (is (empty? (live-cells (engine/step (board 4 3 [[1 1]])))))
  (is (empty? (live-cells (engine/step (board 4 3 []))))))

(deftest boundaries
  (testing "Left/right edges never leak into adjacent rows"
    (let [state (board 5 3 [[4 0] [4 1] [4 2]])]
      (is (= 0 (engine/neighbour-count state 0 1)))
      (is (false? (engine/alive-at? state -1 1)))
      (is (false? (engine/alive-at? state 5 0)))
      (is (= #{[3 1] [4 1]} (live-cells (engine/step state))))))
  (testing "Wraparound wraps each coordinate independently, including corners"
    (let [state (board 5 3 [[4 0] [4 1] [4 2]] :wrap)]
      (is (= 3 (engine/neighbour-count state 0 1)))
      (is (true? (engine/alive-at? state -1 -1)))))
  (testing "Rectangular boards preserve dimensions and population storage"
    (is (= 15 (count (:board (engine/step (board 5 3 [[2 1]]))))))))

(deftest state-and-custom-rules
  (let [state (board 3 2 [[1 1]])
        next-state (engine/step state (fn [_ _] true))]
    (is (= 0 (:generation state)))
    (is (= 1 (:generation next-state)))
    (is (= 6 (count (live-cells next-state))))
    (is (= #{[1 1]} (live-cells state)))))

(deftest input-validation
  (is (thrown? #?(:clj Exception :cljs js/Error) (board 0 3 [])))
  (is (thrown? #?(:clj Exception :cljs js/Error) (board 3 3 [[3 0]])))
  (is (thrown? #?(:clj Exception :cljs js/Error) (board 3 3 [] :unknown)))
  (is (thrown? #?(:clj Exception :cljs js/Error)
               (engine/step (assoc (board 3 3 []) :board [false]))))
  (is (thrown? #?(:clj Exception :cljs js/Error)
               (engine/step (board 3 3 []) (fn [_ _] nil)))))

(deftest original-seed
  (let [state (engine/new-state patterns/original)]
    (is (= 3600 (count (:board state))))
    (is (= (set (:cells patterns/original)) (live-cells state)))
    (is (= 100 (:generation (nth (iterate engine/step state) 100))))))
