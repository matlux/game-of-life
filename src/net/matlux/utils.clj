(ns net.matlux.utils)

(defmacro dbg[x] `(let [x# ~x] (println "dbg:" '~x "=" x#) x#))

(defn unfold [g seed]
  (->> (g seed)
       (iterate (comp g second))
       (take-while identity)
       (map first)))

(defmacro display-assert [x & args] `(let [x# ~x] (if x# true (do (println '~x "is wrong because =" x# "and [" '~@args "] = [" ~@args "]") false))))

(defmacro profile
  "Evaluates exprs in a context in which *out* is bound to a fresh
  StringWriter.  Returns the assoced map with :time -> created by any nested printing
  calls."
  [& body]
  `(let [s# (new java.io.StringWriter)
         oldout# *out*
         res# (binding [*out* s#]
                (time (binding [*out* oldout#] ~@body)))]
     (println "" '~@body "=" (str s#))
     res#))
