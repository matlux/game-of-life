# game-of-life

A Clojure implementation of the famous Conway's game of life.

It's implemented in 60 x 60 matrix and displays in multicolor using Quil. [this project](https://raw.githubusercontent.com/matlux/game-of-life/master/docs/images/qrcode.37839690.png)

## How to run it locally

    lein run
    
## How to require the cellular automaton framework

    (require '[zone.lambda.game.board :as b :refer [DEAD ALIVE]])

## Result

![game of life animated image](https://raw.githubusercontent.com/matlux/game-of-life/master/docs/images/game-of-life.gif)

## rules of the game

```clojure
(defn neighbour-cells [block]
  (concat (subvec (vec block) 0 4) (subvec (vec  block) 5 9)))
(defn neighbour-count [block]
  (count (filter #(= % ALIVE) (neighbour-cells block))))
(defn is-alive? [board i]
  (= (get board i) ALIVE))
(defn coords2state [board coords]
  (map #(get board %) coords))

(defn parse-block [board block-coords]
  (let [i (get block-coords 4)
        ncount (neighbour-count (coords2state board block-coords))]
    (if (is-alive? board i)
      (cond
        (< ncount 2) DEAD
        (or (= ncount 2) (= ncount 3)) ALIVE
        (> ncount 3) DEAD)
      (if (= ncount 3)
        ALIVE
        DEAD))))

(defn apply-rules [board]
  (mapv (fn [i]
          (let [mov (partial b/move i)] (parse-block board [(mov -1 -1) (mov 0 -1) (mov +1 -1)
                                                            (mov -1 0) i           (mov +1 0)
                                                            (mov -1 +1) (mov 0 +1) (mov +1 +1)])))
        (range (* b/column-nb b/raw-nb))))

(defn game-of-life-step [{:keys [board] :as state}]
  (reset! b/arena board)
  (let [new-board (apply-rules board)]
    {:board new-board}))
```

## if you want to modify the colour

modify the code and re-run `lein run`

## if you want to modify the initial state

modify the code and re-run `lein run`

## if you want to modify the rules

modify the code and re-run `lein run`

[or use Klipse for an interactive session :)](http://matlux.github.io/game-of-life)

## License

Copyright © 2016 Mathieu Gauthron

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
