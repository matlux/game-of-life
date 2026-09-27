(ns game-of-life.engine
  (:require [game-of-life.rules :as rules]))

(defn- valid-dimensions? [width height]
  (and (integer? width) (pos? width) (integer? height) (pos? height)))

(defn- valid-boundary? [boundary]
  (contains? #{:dead :wrap} boundary))

(defn new-state
  "Create a finite board from live [x y] coordinates. Edges are dead by default."
  ([pattern] (new-state pattern :dead))
  ([{:keys [width height cells]} boundary]
   (when-not (and (valid-dimensions? width height) (valid-boundary? boundary))
     (throw (ex-info "Use positive integer dimensions and :dead or :wrap edges." {})))
   {:width width :height height :boundary boundary :generation 0
    :board (reduce (fn [board [x y]]
                     (when-not (and (integer? x) (integer? y)
                                    (<= 0 x) (< x width) (<= 0 y) (< y height))
                       (throw (ex-info "Pattern coordinate is outside the board." {:x x :y y})))
                     (assoc board (+ x (* width y)) true))
                   (vec (repeat (* width height) false)) cells)}))

(defn alive-at? [{:keys [width height boundary board]} x y]
  (if (= :wrap boundary)
    (get board (+ (mod x width) (* width (mod y height))))
    (if (and (<= 0 x) (< x width) (<= 0 y) (< y height))
      (get board (+ x (* width y)))
      false)))

(def offsets [[-1 -1] [0 -1] [1 -1] [-1 0] [1 0] [-1 1] [0 1] [1 1]])

(defn neighbour-count [state x y]
  (reduce (fn [n [dx dy]] (if (alive-at? state (+ x dx) (+ y dy)) (inc n) n))
          0 offsets))

(defn step
  "Advance once without side effects. A rule takes [alive? neighbours] and returns a boolean."
  ([state] (step state rules/conway))
  ([{:keys [width height board boundary] :as state} rule]
   (when-not (and (valid-dimensions? width height) (valid-boundary? boundary)
                  (vector? board) (= (count board) (* width height))
                  (every? #(or (true? %) (false? %)) board))
     (throw (ex-info "Invalid board state." {})))
   (assoc state
          :generation (inc (:generation state 0))
          :board (mapv (fn [i]
                         (let [next-cell (rule (get board i)
                                               (neighbour-count state (mod i width) (quot i width)))]
                           (when-not (or (true? next-cell) (false? next-cell))
                             (throw (ex-info "The rule must return true or false." {:cell i})))
                           next-cell))
                       (range (* width height))))))
