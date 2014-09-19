(ns zone.lambda.game.board
  (:require
   [clojure.math.numeric-tower :as math]
   [net.matlux.utils :refer [unfold dbg display-assert]])
  (:import clojure.lang.PersistentVector))

(def ^:dynamic *file-key* \a)
(def ^:dynamic *rank-key* \0)

(def BLANK :.)

(defn c2dto1d [column-nb v]
  (let [[x y] v]
    (+ x (* column-nb y))))

(defn c1dto2d [column-nb i]
  (vector (mod i column-nb) (int (/ i column-nb))))

(defn file-component [file]
  ;{:post [(and (< % 8) (>= % 0))]}
  (- (int file) (int *file-key*)))

(defn rank-component [column-nb rank]
  ;;{:post [(display-assert (and (< % (* raw-nb column-nb)) (>= % 0)) (int (.charAt (name rank) 0)))]}
  (->> (int *rank-key*)
       (- (int rank))
       (- column-nb)
       (* column-nb)))

(defn- file2coord [file]
  ;{:post [(and (< % 8) (>= % 0))]}
  (file-component file))

(defn rank2coord [column-nb rank]
  ;{:post [(and (< % 8) (>= % 0))]}
  (->> (int *rank-key*)
       (- (int rank))
       (- column-nb)))

(defn- coord2file [column-nb x]
  {:pre [(display-assert (and (< x column-nb) (>= x 0)) column-nb x)]}
  (->> (int *file-key*)
       (+ x)
       char))

(defn- coord2rank [column-nb y]
  {:pre [(and (< y column-nb) (>= y 0))]}
  (->> (- (int *rank-key*) y)
       (+ column-nb)
       char))

(defn pos2coord [column-nb ^String pos]
  {:pre [(display-assert (and (string? pos) (= (count pos) 2)) pos)]}
  (let [[file rank] pos
        x (file2coord file)
        y (rank2coord column-nb rank)]
    [x y]))


(defn coord2pos [column-nb [x y]]
  {:pre [(display-assert (and (number? x)(number? y)))]}
  (let [
        file (coord2file column-nb x)
        rank (coord2rank column-nb y)]
    (str file rank)))


(defn- index [column-nb file rank]
  ;;{:pre [(and (char? (.charAt (name file) 0)) (char? (.charAt (name rank) 0)))]}
  (+ (file-component file) (rank-component column-nb rank)))

(defn index-xy [column-nb x y]
  (+ x (* y column-nb)))

(defn- is-vertical? [[x1 y1] [x2 y2]]
  (zero? (- x1 x2)))

(defn- pos-between-vertical [[x1 y1] [x2 y2]]
  (let [[b1 b2] (if (= (.compareTo y2 y1) 1) [y1 y2] [y2 y1])]
      (for [a (range (inc b1) b2)] [x1 a])))

(defn pos-between-xy [[x1 y1] [x2 y2]]
  {:pre [(let [absslop (math/abs (/ (- y2 y1) (- x2 x1)))]
           ;(println absslop)
           (or (= absslop 1)
               (= absslop 0)))]}
  (let [forward? (> (- x2 x1) 0)
          slop (/ (- y2 y1) (- x2 x1))
          [step a b] (if forward? [1 x1 y1] [-1 x2 y2])
         f (fn [x] [(+ a (* 1 x)) (+ b (* slop x))])]
      (map f (range 1 (math/abs(- x2 x1))))))

(defn pos-between [p1 p2]
  (if (is-vertical? p1 p2)
    (pos-between-vertical p1 p2)
    (pos-between-xy p1 p2)))

(defn pos-between-1d [column-nb p1 p2]
  (map #(c2dto1d column-nb %) (pos-between p1 p2)))


(defn is-white? [piece]
  {:pre [(display-assert (char? (.charAt (name piece) 0)) piece)]}
  (Character/isUpperCase (.charAt (name piece) 0)))
(defn is-black? [piece]
  (Character/isLowerCase (.charAt (name piece) 0)))
(defn is-piece? [piece]
  (Character/isLetter (.charAt (name piece) 0)))

(defn lookup [column-nb ^PersistentVector board ^String pos]
  {:pre [(string? pos)]}
  (let [[file rank] pos]
    (board (index column-nb file rank))))
(defn lookup-xy [column-nb ^PersistentVector board ^PersistentVector pos]
  {:pre [(display-assert (and (vector? pos) (number? (first pos))) pos)]}
  (lookup column-nb board (coord2pos column-nb pos)))

(defn nothing-between [column-nb board p1 p2]
  (not-any? is-piece? (map #(lookup-xy column-nb board %) (pos-between p1 p2))))

(defn board2xy-map-piece [raw-nb column-nb pieces-list]
  (into {}
        (filter
         #(not= BLANK (second %))
         (map
          #(vector (c1dto2d column-nb %1) %2 )
          (range (* column-nb raw-nb))
          pieces-list))))

(defn pos-xy-within-board? [raw-nb column-nb [x y]]
  (and (< x raw-nb)
       (>= x 0)
       (< y column-nb)
       (>= y 0)
       ))

(defn- pos-within-board? [column-nb ^String pos]
  (pos-xy-within-board?
   (let [[file rank] pos
         x (file2coord file)
         y (rank2coord column-nb rank)]
     [x y])))

(defn collid-self? [column-nb board is-player1-turn coord]
  (if is-player1-turn
    (is-white? (lookup column-nb board (coord2pos column-nb coord)))
    (is-black? (lookup column-nb board (coord2pos column-nb coord)))))
(defn collid-oposite? [column-nb board is-player1-turn coord]
  (if is-player1-turn
    (is-black? (lookup column-nb board (coord2pos column-nb coord)))
    (is-white? (lookup column-nb board (coord2pos column-nb coord)))
    ))

(defn collid? [column-nb board pos] (not (= (lookup-xy column-nb board pos) :.)))

(defn generate-line [n]
  (apply str "+" (repeat n "---+")))

(generate-line 7)

(defn render-board [raw-nb column-nb board-state]
  (let [line (generate-line column-nb)
        pieces-pos board-state ;(into {} board-state)
        ]
    (apply str "\n" line "\n"
           (map #(let [pos (c1dto2d column-nb (dec %))
                       c (name (get pieces-pos pos " "))]
                   (if (zero? (mod % column-nb))
                           (format "| %s |\n%s\n" c line)
                           (format "| %s " c))) (range 1 (inc (* column-nb raw-nb)))))))


(defn display-board [raw-nb column-nb board]
  (println (render-board raw-nb column-nb (board2xy-map-piece raw-nb column-nb board))))
