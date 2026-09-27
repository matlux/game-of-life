(ns game-of-life.rules)

(defn conway
  "Return a boolean for the next cell state, given its state and neighbour count."
  [alive? neighbours]
  (or (= neighbours 3)
      (and alive? (= neighbours 2))))
