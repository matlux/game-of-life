(ns game-of-life.desktop
  (:require [game-of-life.engine :as engine]
            [game-of-life.patterns :as patterns]
            [quil.core :as q]
            [quil.middleware :as middleware]))

(def cell-size 10)

(defn initial-state []
  (assoc (engine/new-state patterns/original) :paused? false))

(defn setup []
  (q/frame-rate 10)
  (q/color-mode :hsb)
  (q/no-stroke)
  (initial-state))

(defn update-state [state]
  (if (:paused? state) state (engine/step state)))

(defn key-pressed [state {:keys [key]}]
  (case key
    :space (update state :paused? not)
    :r (initial-state)
    :n (engine/step (assoc state :paused? true))
    state))

(defn draw [{:keys [width board]}]
  (q/background 0)
  (doseq [i (range (count board)) :when (get board i)]
    (q/fill (+ 30 (mod i 75)) 255 255)
    (q/rect (* cell-size (mod i width)) (* cell-size (quot i width)) cell-size cell-size)))

(defn mouse-pressed [{:keys [width height] :as state} {:keys [x y button]}]
  (let [column (int (Math/floor (/ x cell-size)))
        row (int (Math/floor (/ y cell-size)))]
    (if (and (= button :left) (<= 0 column) (< column width) (<= 0 row) (< row height))
      (update-in state [:board (+ column (* width row))] not)
      state)))

(defn launch!
  ([] (launch! {}))
  ([options]
   (apply q/sketch
          (mapcat identity
                  (merge {:title "Game of Life — Space: pause · N: step · R: reset"
                          :size [(* cell-size (:width patterns/original))
                                 (* cell-size (:height patterns/original))]
                          :setup setup :update update-state :draw draw :key-pressed key-pressed
                          :mouse-pressed mouse-pressed
                          :middleware [middleware/fun-mode]
                          :features [:exit-on-close]}
                         options)))))
