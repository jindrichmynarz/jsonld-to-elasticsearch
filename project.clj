(defproject jsonld-to-elasticsearch "0.1.0"
  :description "Loads line-separated JSON-LD documents into Elasticsearch"
  :url "http://github.com/jindrichmynarz/jsonld-to-elasticsearch"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clojurewerkz/elastisch "3.0.0-beta1"]
                 [prismatic/schema "1.1.3"]
                 [mount "0.1.10"]
                 [slingshot "0.12.2"]
                 [clj-http "3.3.0"]
                 [org.clojure/tools.cli "0.3.5"]
                 [cheshire "5.6.3"]
                 [commons-validator/commons-validator "1.5.1"]]
  :main jsonld-to-elasticsearch.core
  :profiles {:uberjar {:aot :all
                       :uberjar-name "jsonld_to_elasticsearch.jar"}})
