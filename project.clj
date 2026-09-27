(defproject game-of-life "0.1.0-SNAPSHOT"
  :description "Game of Life on the JVM and in a self-hosted ClojureScript playground"
  :url "https://github.com/matlux/game-of-life"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.12.6"]
                 ;; Only the 2D desktop renderer is used. Omit browser, PDF, SVG,
                 ;; and DXF exporters and their old transitive libraries.
                 [quil "4.3.1563"
                  :exclusions [org.clojure/clojure cljsjs/p5
                               quil/processing-pdf quil/processing-svg quil/processing-dxf
                               com.lowagie/itext org.bouncycastle/bctsp-jdk14
                               org.apache.xmlgraphics/batik-dom
                               org.apache.xmlgraphics/batik-svggen]]]
  :main game-of-life.core
  :jvm-opts ["--enable-native-access=ALL-UNNAMED"]
  :profiles {:smoke {:source-paths ["dev"]}}
  :aliases {"desktop-smoke" ["with-profile" "+smoke" "run" "-m" "game-of-life.desktop-smoke"]})
