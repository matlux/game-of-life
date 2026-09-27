(ns game-of-life.build
  (:require [cljs.build.api :as cljs]
            [clojure.java.io :as io]
            [clojure.pprint :as pprint]
            [clojure.data.json :as json]
            [game-of-life.engine :as engine]
            [game-of-life.patterns :as patterns]))

(defn rule-source []
  ;; Generate the editable expression from the same function the JVM compiles.
  (with-open [reader (java.io.PushbackReader. (io/reader "src/game_of_life/rules.cljc"))]
    (let [forms (take-while some? (repeatedly #(read {:eof nil} reader)))
          definition (first (filter #(and (seq? %) (= 'defn (first %)) (= 'conway (second %))) forms))
          declarations (drop 2 definition)
          declarations (if (string? (first declarations)) (rest declarations) declarations)]
      (when-not definition (throw (ex-info "Cannot find canonical Conway rule." {})))
      (with-out-str
        (pprint/with-pprint-dispatch pprint/code-dispatch
          (pprint/pprint (cons 'fn declarations)))))))

(defn write-file! [path content]
  (io/make-parents path)
  (spit path content))

(defn site! []
  (doseq [file ["index.html" "style.css" "app.mjs" "worker-client.mjs"]]
    (let [target (io/file "target/site" file)]
      (io/make-parents target)
      (io/copy (io/file "web" file) target)))
  (write-file! "target/site/default-rule.cljs" (rule-source))
  (write-file! "target/site/patterns.json"
               (json/write-str (map #(select-keys % [:id :name :description]) patterns/catalog)))
  (write-file! "target/site/.nojekyll" "")
  (cljs/build "src" {:main 'game-of-life.worker :target :webworker
                     :output-to "target/site/js/worker.js" :output-dir "target/worker"
                     ;; Runtime eval needs namespace names; advanced optimization is incompatible.
                     :optimizations :simple :pretty-print false :static-fns false
                     :parallel-build true})
  (println "Static website built in target/site"))

(defn tests! []
  (write-file! "target/test-fixtures.json"
               (json/write-str
                {:rule-source (rule-source)
                 :trajectories (into {}
                                     (for [boundary [:dead :wrap]]
                                       [boundary (vec (take 21 (iterate engine/step
                                                                       (engine/new-state patterns/original boundary))))]))}))
  (cljs/build (cljs/inputs "src" "test")
              {:main 'game-of-life.test-runner :target :nodejs
               :output-to "target/cljs-tests.js" :output-dir "target/cljs-tests"
               :optimizations :none})
  (println "Run node target/cljs-tests.js and node --test test/web/*.test.mjs"))

(defn -main [target]
  (case target "site" (site!) "test" (tests!)
        (throw (ex-info "Expected site or test." {:target target})))
  (shutdown-agents))
