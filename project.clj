(defproject karriboard "0.1.0-SNAPSHOT"
  :description "the imageboard"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url ""}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.6.0"]
                 [hiccup "1.0.5"]
                 [ring "1.5.0"]
                 [ring/ring-json "0.1.2"]
                 [cheshire "4.0.3"]
                 [c3p0/c3p0 "0.9.1.2"]
                 [javax.servlet/servlet-api "2.5"]
                 [ring/ring-jetty-adapter "1.4.0"]
                 [org.clojure/java.jdbc "0.7.3"]
                 [org.clojure/core.async "0.3.443"]
                 [org.clojure/data.json "0.2.6"]
                 [peridot "0.5.0"]
                 [org.postgresql/postgresql "42.0.0"]]
  :main ^:skip-aot karriboard.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all} :dev {:plugins [[lein-ring "0.10.0"]]}})
