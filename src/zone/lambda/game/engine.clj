(ns zone.lambda.game.engine
  (:require
   [net.matlux.utils :refer [unfold dbg]]
   [zone.lambda.game.board :refer [display-board]]
   [clojure.algo.monads :as m :refer [domonad state-m fetch-state fetch-val]]))

(defn interactive-player [display-board]
  (fn [{board :board is-player1-turn :is-player1-turn}]
     (display-board board)
     (let [move (read-string (read-line))]
       {:move move})))

;;(interactive-player {:board initial-board :is-player1-turn true :player1 initial-board :player2 initial-board})

(defn game-step-monad-wrap [game-step]
  (domonad state-m
           [res (fetch-val :result)
            a game-step
            s (fetch-state)
            :when (nil? res)]
           [a s]

           ))

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

;;(take 8 (play-game-seq game-step {:board initial-board :is-player1-turn true :player1 (create-fn [0 1 2 3]) :player2 (create-fn [0 1 2 4]) }))
