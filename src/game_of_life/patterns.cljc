(ns game-of-life.patterns)

;; Live coordinates from the original 2016 60 x 60 seed, stored once for both runtimes.
(def original
  {:width 60 :height 60
   :cells [
    [25 2] [23 3] [25 3] [13 4] [14 4] [21 4] [22 4] [35 4]
    [36 4] [12 5] [16 5] [21 5] [22 5] [35 5] [36 5] [1 6]
    [2 6] [11 6] [17 6] [21 6] [22 6] [1 7] [2 7] [11 7]
    [15 7] [17 7] [18 7] [23 7] [25 7] [11 8] [17 8] [25 8]
    [12 9] [16 9] [13 10] [14 10] [18 29] [14 30] [17 30] [20 30]
    [15 31] [16 31] [17 31] [18 31] [19 31] [15 32] [17 32] [18 32]
    [13 49] [14 49] [15 49] [16 49] [17 49] [18 49] [19 49] [20 49]
    [14 50] [17 50] [14 51] [16 51] [17 51] [18 51] [29 51] [34 51]
    [13 52] [14 52] [27 52] [29 52] [32 52] [34 52] [4 53] [13 53]
    [15 53] [28 53] [29 53] [33 53] [34 53] [3 54] [5 54] [12 54]
    [14 54] [17 54] [3 55] [5 55] [11 55] [17 55] [4 56] [7 56]
    [17 56] [2 57] [6 57] [8 57] [2 58] [6 58] [8 58] [2 59]
    [7 59]]})

(def glider
  {:width 60 :height 60 :cells [[2 1] [3 2] [1 3] [2 3] [3 3]]})

(def blinker
  {:width 60 :height 60 :cells [[29 30] [30 30] [31 30]]})

(defn- centered [rows]
  (let [left (quot (- 60 (apply max (map count rows))) 2)
        top (quot (- 60 (count rows)) 2)]
    {:width 60 :height 60
     :cells (vec (for [[y row] (map-indexed vector rows)
                       [x cell] (map-indexed vector row)
                       :when (= cell \O)]
                   [(+ left x) (+ top y)]))}))

;; Standard Conway patterns; see https://conwaylife.com/wiki/Category:Patterns.
;; Keep the seeds as readable cell diagrams, compiled for both runtimes.
(def pulsar
  (centered ["..OOO...OOO.." "............." "O....O.O....O"
             "O....O.O....O" "O....O.O....O" "..OOO...OOO.."
             "............." "..OOO...OOO.." "O....O.O....O"
             "O....O.O....O" "O....O.O....O" "............."
             "..OOO...OOO.."]))

(def pentadecathlon
  (centered ["..O....O.." "OO.OOOO.OO" "..O....O.."]))

(def lightweight-spaceship
  (centered [".O..O" "O...." "O...O" "OOOO."]))

(def acorn (centered [".O....." "...O..." "OO..OOO"]))
(def diehard (centered ["......O." "OO......" ".O...OOO"]))
(def blank {:width 60 :height 60 :cells []})

(def gosper-glider-gun
  {:width 60 :height 60
   :cells (mapv (fn [[x y]] [(+ 5 x) (+ 5 y)])
                [[24 0] [22 1] [24 1] [12 2] [13 2] [20 2] [21 2] [34 2] [35 2]
                 [11 3] [15 3] [20 3] [21 3] [34 3] [35 3]
                 [0 4] [1 4] [10 4] [16 4] [20 4] [21 4]
                 [0 5] [1 5] [10 5] [14 5] [16 5] [17 5] [22 5] [24 5]
                 [10 6] [16 6] [24 6] [11 7] [15 7] [12 8] [13 8]])})

(def catalog
  [{:id :original :name "Original 2016 seed" :pattern original
    :description "The original collection of interacting seeds."}
   {:id :glider :name "Glider" :pattern glider
    :description "A five-cell spaceship that travels diagonally: one cell every four generations."}
   {:id :blinker :name "Blinker" :pattern blinker
    :description "The simplest oscillator: flips between horizontal and vertical every generation."}
   {:id :pulsar :name "Pulsar" :pattern pulsar
    :description "A symmetric oscillator that returns to its starting shape every three generations."}
   {:id :pentadecathlon :name "Pentadecathlon" :pattern pentadecathlon
    :description "A long, transforming oscillator with a 15-generation cycle."}
   {:id :lightweight-spaceship :name "Lightweight spaceship" :pattern lightweight-spaceship
    :description "A nine-cell spaceship that travels horizontally: two cells every four generations."}
   {:id :gosper-glider-gun :name "Gosper glider gun" :pattern gosper-glider-gun
    :description "A repeating machine that emits a new glider every 30 generations. Try intercepting its stream."}
   {:id :acorn :name "Acorn" :pattern acorn
    :description "Just seven starting cells grow into a long, chaotic evolution. On this finite board, the edges affect its eventual fate."}
   {:id :diehard :name "Diehard" :pattern diehard
    :description "Seven cells transform repeatedly, then disappear at generation 130 with Conway’s rules and dead edges."}
   {:id :blank :name "Empty board" :pattern blank
    :description "Your canvas: add cells to create a pattern of your own."}])

(def all (into {} (map (juxt :id :pattern) catalog)))
