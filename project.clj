(defproject lisa "0.1.0-SNAPSHOT"
  :description "Openflow implementation in clojure"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies  [[aleph "0.4.0-SNAPSHOT"]
                 [manifold "0.1.0-SNAPSHOT"]
                 [gloss "0.2.4"]
                 ;;  [compojure "1.3.1"]
                 [org.clojure/core.match "0.3.0-alpha4"]
               ;;  [clojurewerkz/neocons "3.0.0"]
               ;;  [com.taoensso/carmine "2.9.0"] 
               ;;  [com.taoensso/timbre "3.3.1"]
                 [org.clojure/clojure "1.7.0-alpha5"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [org.clojure/tools.cli "0.3.1"]]
  :plugins [[lein-marginalia "0.8.0"]]  
  :main lisa.l2switch)
