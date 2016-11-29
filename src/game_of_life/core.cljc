(ns game-of-life.core
  #?(:clj (:require
            [net.matlux.utils :refer [display-assert]]
            [quil.core :as q])
  ))



;; board logic
(def test-board
  [
    :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
    :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
    :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :X :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
    :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :X :. :X :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
    :. :. :. :. :. :. :. :. :. :. :. :. :. :X :X :. :. :. :. :. :. :X :X :. :. :. :. :. :. :. :. :. :. :. :. :X :X :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
    :. :. :. :. :. :. :. :. :. :. :. :. :X :. :. :. :X :. :. :. :. :X :X :. :. :. :. :. :. :. :. :. :. :. :. :X :X :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
    :. :X :X :. :. :. :. :. :. :. :. :X :. :. :. :. :. :X :. :. :. :X :X :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
    :. :X :X :. :. :. :. :. :. :. :. :X :. :. :. :X :. :X :X :. :. :. :. :X :. :X :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
    :. :. :. :. :. :. :. :. :. :. :. :X :. :. :. :. :. :X :. :. :. :. :. :. :. :X :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
    :. :. :. :. :. :. :. :. :. :. :. :. :X :. :. :. :X :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.

    :. :. :. :. :. :. :. :. :. :. :. :. :. :X :X :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
    :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
    :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
    :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
    :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
    :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
    :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
    :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
    :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
    :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
    :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.

    :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
    :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
    :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
    :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
    :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
    :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
    :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
    :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
    :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :X :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
    :. :. :. :. :. :. :. :. :. :. :. :. :. :. :X :. :. :X :. :. :X :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
    :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :X :X :X :X :X :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
    :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :X :. :X :X :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
    :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
    :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
    :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
    :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
    :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
    :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
    :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
    :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.

    :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
    :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
    :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
    :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
    :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
    :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
    :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
    :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
    :. :. :. :. :. :. :. :. :. :. :. :. :. :X :X :X :X :X :X :X :X :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
    :. :. :. :. :. :. :. :. :. :. :. :. :. :. :X :. :. :X :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
    :. :. :. :. :. :. :. :. :. :. :. :. :. :. :X :. :X :X :X :. :. :. :. :. :. :. :. :. :. :X :. :. :. :. :X :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
    :. :. :. :. :. :. :. :. :. :. :. :. :. :X :X :. :. :. :. :. :. :. :. :. :. :. :. :X :. :X :. :. :X :. :X :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
    :. :. :. :. :X :. :. :. :. :. :. :. :. :X :. :X :. :. :. :. :. :. :. :. :. :. :. :. :X :X :. :. :. :X :X :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
    :. :. :. :X :. :X :. :. :. :. :. :. :X :. :X :. :. :X :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
    :. :. :. :X :. :X :. :. :. :. :. :X :. :. :. :. :. :X :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
    :. :. :. :. :X :. :. :X :. :. :. :. :. :. :. :. :. :X :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
    :. :. :X :. :. :. :X :. :X :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
    :. :. :X :. :. :. :X :. :X :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.
    :. :. :X :. :. :. :. :X :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :. :.


    ])

(defn abs [n]
  (if (< n 0)
    (- 0 n)
    n))

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
  ;{:pre [(display-assert (and (< x column-nb) (>= x 0)) column-nb x)]}
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
  ;{:pre [(display-assert (and (number? x)(number? y)))]}
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
  {:pre [(let [absslop (abs (/ (- y2 y1) (- x2 x1)))]
           ;(println absslop)
           (or (= absslop 1)
               (= absslop 0)))]}
  (let [forward? (> (- x2 x1) 0)
        slop (/ (- y2 y1) (- x2 x1))
        [step a b] (if forward? [1 x1 y1] [-1 x2 y2])
        f (fn [x] [(+ a (* 1 x)) (+ b (* slop x))])]
    (map f (range 1 (abs(- x2 x1))))))

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

(defn lookup [column-nb board ^String pos]
  {:pre [(string? pos)]}
  (let [[file rank] pos]
    (board (index column-nb file rank))))
(defn lookup-xy [column-nb board pos]
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

(defn render-board-no-border [raw-nb column-nb board-state]
  (let [line (generate-line column-nb)
        pieces-pos board-state ;(into {} board-state)
        ]
    (apply str "\n"
           (map #(let [pos (c1dto2d column-nb (dec %))
                       c (name (get pieces-pos pos " "))]
                   (if (zero? (mod % column-nb))
                     (format "%s \n" c)
                     (format "%s " c))) (range 1 (inc (* column-nb raw-nb)))))))


(defn display-board [raw-nb column-nb board]
  (println (render-board raw-nb column-nb (board2xy-map-piece raw-nb column-nb board))))

(defn display-board-no-border [raw-nb column-nb board]
  (println (render-board-no-border raw-nb column-nb (board2xy-map-piece raw-nb column-nb board))))

(defn unfold [g seed]
  (->> (g seed)
       (iterate (comp g second))
       (take-while identity)
       (map first)))

(defn interactive-player [display-board]
  (fn [{board :board is-player1-turn :is-player1-turn}]
    (display-board board)
    (let [move (read-string (read-line))]
      {:move move})))

;; this function is central to the chess engine
(defn game-seq [monadic-step init-state]
  (unfold
    monadic-step
    init-state
    ))

(defn play-game-seq [step game-init]
  (let [state (merge {:is-player1-turn true :game-id (str (java.util.UUID/randomUUID))} game-init)]
    (game-seq step state)))


(defn- every-nth [coll n]
  (map (fn [[i e]] e) (filter (fn [[i e]] (zero? (mod i n))) (map-indexed (fn [i e] [i e]) coll))))

(defn- create-fn [moves]
  (fn [{b :board c :is-player1-turn s :state}]
    (let [move-seq (if (nil? s)
                     moves
                     s)]
      {:move (first move-seq) :state (next move-seq)})))


;;((create-fn [0 1 2 4]) {:board initial-board :is-player1-turn true})

(defn create-fns-from-scenario [moves]
  (let [white-moves (every-nth moves 2)
        black-moves (every-nth (drop 1 moves) 2)]
    [(create-fn white-moves)
     (create-fn black-moves)]))

(defn play-scenario-seq [initial-board step scenario] (let [[f1 f2] (create-fns-from-scenario scenario)]
                                                        (let [result (play-game-seq step {:board initial-board :f1 f1 :f2 f2})]
                                                          (take (count scenario) result))))

(defn seq-result [s]
  (-> s
      last
      second))

(def column-nb 60)
(def raw-nb 60)
(def c2dto1d (partial c2dto1d column-nb))
(def c1dto2d (partial c1dto2d column-nb))

(def display (partial display-board-no-border raw-nb column-nb))

(def initial-board
  (into [] (map (fn [_] :.) (range (* column-nb raw-nb)))))

;; Quil stuff
(def arena (atom test-board))
;;(reset! arena test-board)

(def size "size of the square arena" column-nb)
(def scale 10)
(def sleep-length "time in ms between turns" 100)

#?(:cljs (defn draw []
               (doseq [x (range 0 size)
                       y (range 0 size)]
                      (when-let [hue (if (= (get @arena (c2dto1d [x y])) :X) (+ 30 (int (rand 75))))]

                                (rect (* x scale) (* y scale) (if hue "green" "dodgerblue"))))))

(def test-board2
  [:. :. :. :. :.
   :. :. :X :. :.
   :. :. :X :. :.
   :. :. :X :. :.
   :. :. :. :. :.
   ])

(defn move [i xd yd]
  (let [[x y] (c1dto2d i)]
    (c2dto1d [(+ x xd) (+ y yd)])))

;;(move 0 0 0)

(defn left [i] (move i -1 0))
(defn right [i] (move i +1 0))
(defn up [i] (move i 0 -1))
(defn down [i] (move i 0 +1))

(def test-block
  [:. :. :.
   :X :X :X
   :. :. :.
   ])

(defn neighbour-cells [block]
  (concat (subvec (vec block) 0 4) (subvec (vec  block) 5 9)))

(defn neighbour-count [block]
  (count (filter #(= % :X) (neighbour-cells block))))


(defn is-alive? [board i]
  (= (get board i) :X))

(is-alive? test-board (c2dto1d [-1 -1]))

(defn coords2state [board coords]
  (map #(get board %) coords))

;; (coords2state test-board [6  7  8
;;                           11 12 13
;;                           16 17 18])

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

#?(:cljs (defn rect [x1 y1 color]
  (let [canvas (js/document.getElementById @canvas)
        ctx (.getContext canvas "2d")
        w (.-width canvas)
        h (.-height canvas)]
    ;(.clearRect ctx x1 y1 x2 y2)
    ;(set! (.-fillStyle ctx) "dodgerblue")
    (set! (.-fillStyle ctx) color)
    (.fillRect ctx x1 y1 scale  scale)

    ))
)

(defn apply-rules [board]
  (mapv (fn [i]
         (let [mov (partial move i)] (parse-block board [(mov -1 -1) (mov 0 -1) (mov +1 -1)
                                                         (mov -1 0) i           (mov +1 0)
                                                         (mov -1 +1) (mov 0 +1) (mov +1 +1)])))
       (range (* column-nb raw-nb))))

(defn game-of-life-step [{:keys [board] :as state}]
  ;;(display board)
  (reset! arena board)
  (let [new-board (apply-rules board)]
    {:board new-board}))


#?(:clj
   (do
     (defn blank-arena []
       (dosync
         (doseq [row arena r row]
           (ref-set r nil))))

     (defn setup []
       (q/color-mode :hsb)
       (q/smooth)
       (q/frame-rate 10))

     (defn draw []
       (q/background 0)
       (dosync
         (doseq [x (range 0 size)
                 y (range 0 size)]
           (when-let [hue (if (= (get @arena (c2dto1d [x y])) :X) (+ 30 (int (rand 75))))]
             (q/fill (q/color hue 255 255))
             (q/rect (* scale x) (* scale y) scale scale)))))

     (q/defsketch gameoflife
                  :title "game of life"
                  :setup setup
                  :draw draw
                  :size [(* scale size) (* scale size)])


     (defn play []
       (dorun (iterate game-of-life-step {:board test-board})))




     (defn -main []
       (play))))



