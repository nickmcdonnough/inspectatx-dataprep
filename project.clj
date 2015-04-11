(defproject dataprep "0.1.0"
  :description "Tool to retrieve csv from city and prepare it to be imported to the database."
  :url "http://github.com/nickmcdonnough/inspectatx"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/java.jdbc "0.3.6"]
                 [postgresql/postgresql "8.4-702.jdbc4"]
                 [org.clojure/data.csv "0.1.2"]
                 [semantic-csv "0.1.0-alpha4"]
                 [clj-http-lite "0.2.1"]]
  :main ^:skip-aot dataprep.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
