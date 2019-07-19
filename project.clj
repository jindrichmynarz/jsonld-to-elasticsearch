(defproject jsonld-to-elasticsearch "0.2.0"
  :description "Loads line-separated JSON-LD documents into Elasticsearch"
  :url "http://github.com/jindrichmynarz/jsonld-to-elasticsearch"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [clojurewerkz/elastisch "3.0.1"]
                 [prismatic/schema "1.1.11"]
                 [mount "0.1.16"]
                 [slingshot "0.12.2"]
                 [clj-http "3.10.0"]
                 [org.clojure/tools.cli "0.4.2"]
                 [cheshire "5.8.1"]
                 [commons-validator/commons-validator "1.6"]]
  :main jsonld-to-elasticsearch.core
  :profiles {:uberjar {:aot :all
                       :uberjar-name "jsonld_to_elasticsearch.jar"}})
