(ns game-of-life.core
  (:require
   [net.matlux.utils :refer [unfold dbg]]
   [zone.lambda.game.board :as board :refer [pos-between BLANK]])
  )


(def column-nb 40)
(def raw-nb 40)
(def c2dto1d (partial board/c2dto1d column-nb))
(def c1dto2d (partial board/c1dto2d column-nb))

(def display (partial board/display-board raw-nb column-nb))

(def initial-board
  (into [] (map (fn [_] :.) (range (* column-nb raw-nb)))))
(def test-board
  [
   :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
   :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
   :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :X :. :. :. :. :. :. :. :. :. :. :. :. :. :.
   :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :X :. :X :. :. :. :. :. :. :. :. :. :. :. :. :. :.
   :. :. :. :. :. :. :. :. :. :. :. :. :. :X :X :. :. :. :. :. :. :X :X :. :. :. :. :. :. :. :. :. :. :. :. :X :X :. :. :.
   :. :. :. :. :. :. :. :. :. :. :. :. :X :. :. :. :X :. :. :. :. :X :X :. :. :. :. :. :. :. :. :. :. :. :. :X :X :. :. :.
   :. :X :X :. :. :. :. :. :. :. :. :X :. :. :. :. :. :X :. :. :. :X :X :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
   :. :X :X :. :. :. :. :. :. :. :. :X :. :. :. :X :. :X :X :. :. :. :. :X :. :X :. :. :. :. :. :. :. :. :. :. :. :. :. :.
   :. :. :. :. :. :. :. :. :. :. :. :X :. :. :. :. :. :X :. :. :. :. :. :. :. :X :. :. :. :. :. :. :. :. :. :. :. :. :. :.
   :. :. :. :. :. :. :. :. :. :. :. :. :X :. :. :. :X :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.

   :. :. :. :. :. :. :. :. :. :. :. :. :. :X :X :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
   :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
   :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
   :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
   :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
   :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
   :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
   :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
   :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
   :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
   :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.

   :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
   :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
   :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
   :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
   :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
   :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
   :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
   :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
   :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
   :. :. :. :. :. :. :. :. :. :. :. :. :. :. :X :. :. :X :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
   :. :. :. :. :. :. :. :. :. :. :. :. :. :. :X :. :X :X :X :. :. :. :. :. :. :. :. :. :. :X :. :. :. :. :X :. :. :. :. :.
   :. :. :. :. :. :. :. :. :. :. :. :. :. :X :X :. :. :. :. :. :. :. :. :. :. :. :. :X :. :X :. :. :X :. :X :. :. :. :. :.
   :. :. :. :. :. :. :. :. :. :. :. :. :. :X :. :X :. :. :. :. :. :. :. :. :. :. :. :. :X :X :. :. :. :X :X :. :. :. :. :.
   :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :X :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.

   :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :X :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
   :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :X :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
   :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
   :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
   :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.


   ])



(def test-board2
  [:. :. :. :. :.
   :. :. :X :. :.
   :. :. :X :. :.
   :. :. :X :. :.
   :. :. :. :. :.
   ])

(comment

  (reduce (fn [state e] new-state) (range 3))

  (get test-board )
  (c1dto2d 4)

  (unfold (fn [state]
            [value new-state]) initial-state)
  )


(defn move [i xd yd]
      (let [[x y] (c1dto2d i)]
        (c2dto1d [(+ x xd) (+ y yd)])))

;;(move 0 0 0)

(defn left [i] (move i -1 0))
(defn right [i] (move i +1 0))
(defn up [i] (move i 0 -1))
(defn down [i] (move i 0 +1))

(left 7)
(right 7)
(up 7)
(down 7)

(def test-block
  [:. :. :.
   :X :X :X
   :. :. :.
   ])

(defn neighbour-cells [block]
  (concat (subvec (vec block) 0 4) (subvec (vec  block) 5 9)))

(defn neighbour-count [block]
  (count (filter #(= % :X) (neighbour-cells block))))

(neighbour-count test-block)

;; (neighbour-count [:. :. :. :X :X :X :. :. :.])
;; (subvec [:. :. :. :X :y :z :. :. :.] 0 4)
;; (subvec [:. :. :. :X :y :z :. :. :.] 5 9)
;;(subvec '(:. :. :. :X :X :X :. :. :.) 0 5)

(defn is-alive? [board i]
  (= (get board i) :X))

(is-alive? test-board (c2dto1d [-1 -1]))

(defn coords2state [board coords]
  (map #(get board %) coords))

(coords2state test-board [6  7  8
                          11 12 13
                          16 17 18])

(defn parse-block [board block-coords]
  (let [i (get block-coords 4)
        ncount (neighbour-count (coords2state board block-coords))]
    (if (is-alive? board i)
      (cond
       (< ncount 2) :.
       (or (= ncount 2) (= ncount 3)) :X
       (> ncount 3) :.)
      (if (= ncount 3)
        :X
        :. ))))

(comment

  (neighbour-count (coords2state test-board test-right))
  (def test-right [7  8 9
                          12 13 14
                          17 18 19])

 (parse-block test-board [6  7  8
                          11 12 13
                          16 17 18])
 (parse-block test-board [7  8 9
                          12 13 14
                          17 18 19])
 )

(defn apply-rules [board]
  (into []  (map (fn [i]
          (let [mov (partial move i)] (parse-block board [(mov -1 -1) (mov 0 -1) (mov +1 -1)
                                                          (mov -1 0) i           (mov +1 0)
                                                          (mov -1 +1) (mov 0 +1) (mov +1 +1)])))
        (range (* column-nb raw-nb)))))

;;(index '( 5 4 3 2))
;;(apply-rules test-board)
(map (fn [i]
                 (let [mov (partial move i)] (parse-block test-board [(mov -1 -1) (mov 0 -1) (mov +1 -1)
                                                           (mov -1 0) i           (mov +1 0)
                                                           (mov -1 +1) (mov 0 +1) (mov +1 +1)])))
       (range (* column-nb raw-nb)))

(defn game-of-life-step [{:keys [board] :as state}]
  (display board)
  (let [new-board (apply-rules board)]
    {:board new-board}))

(comment
  (display (:board (game-of-life-step {:board test-board2})))
  (->> (game-of-life-step {:board test-board}) game-of-life-step game-of-life-step)
  (game-of-life-step (game-of-life-step {:board test-board}))
  (game-of-life-step {:board test-board2})
  (game-of-life-step test1)
  (game-of-life-step test2)



  (def test1 (game-of-life-step {:board test-board}))
  (def test2 {:board test-board2})
  (= (game-of-life-step {:board test-board}) {:board test-board2})
  (= test1 test2))


(defn play []
  (dorun (iterate game-of-life-step {:board test-board})))

(defn -main []
  (play))
