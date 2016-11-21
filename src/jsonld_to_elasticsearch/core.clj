(ns jsonld-to-elasticsearch.core
  (:gen-class)
  (:require [jsonld-to-elasticsearch.util :as util]
            [jsonld-to-elasticsearch.elasticsearch :refer [elasticsearch]]
            [clojure.set :refer [rename-keys]]
            [mount.core :as mount]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.java.io :as io :refer [as-file]]
            [clojure.edn :as edn]
            [schema.core :as s]
            [clojurewerkz.elastisch.rest.bulk :as esb]
            [cheshire.core :as json])
  (:import (java.io BufferedReader Reader)
           (org.apache.commons.validator.routines UrlValidator)))

; ----- Schemata -----

(def ^:private positive-integer (s/constrained s/Int pos? 'pos?))

(def ^:private http? (partial re-matches #"^https?:\/\/.*$"))

(def ^:private valid-url?
  "Test if `url` is valid."
  (let [validator (UrlValidator. UrlValidator/ALLOW_LOCAL_URLS)]
    (fn [url]
      (.isValid validator url))))

(def ^:private url
  (s/pred valid-url? 'valid-url?))

(def ^:private Config
  {:endpoint (s/conditional http? url) ; URL of the Elasticsearch HTTP endpoint
   :index s/Str ; Name of the index to use
   (s/optional-key :batch-size) positive-integer ; Number of documents to index in batch
   })

(def ^:private Mapping
  {:mappings {s/Keyword {s/Keyword s/Any}}
   :settings s/Any})

; ----- Private functions -----

(defn- error-msg
  [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (util/join-lines errors)))

(defn- exit
  "Exit with @status and message `msg`.
  `status` 0 is OK, `status` 1 indicates error."
  [^Integer status
   ^String msg]
  {:pre [(#{0 1} status)]}
  (println msg)
  (System/exit status))

(def ^:private die
  (partial exit 1))

(def ^:private info
  (partial exit 0))

(defn- file-exists?
  "Test if file at `path` exists and is a file."
  [path]
  (let [file (as-file path)]
    (and (.exists file) (.isFile file))))

(defn- usage
  [summary]
  (util/join-lines ["Loads line-separated JSON-LD files to Elasticsearch."
                    "Options:\n"
                    summary]))

(defn- ->schema-validator
  [schema schema-name instance]
  (let [expected-structure (s/explain schema)]
    (try (s/validate schema instance) true
         (catch RuntimeException e (util/join-lines [(format "Invalid %s:" schema-name)
                                                     (.getMessage e)
                                                     (format "The expected structure of the %s is:" schema-name)
                                                     expected-structure])))))

(def ^:private validate-config
  "Validate configuration `config` according to its schema."
  (partial ->schema-validator Config "configuration"))

(def ^:private validate-mapping
  "Validate Elasticsearch `mapping` according to its schema."
  (partial ->schema-validator Mapping "mapping"))

(defn- parse-config
  "Parse EDN configuration from `path`."
  [path]
  {:pre [(file-exists? path)]}
  (edn/read-string (slurp path)))

(defn- parse-input
  "Parse path to `input`."
  [input]
  {:pre [(file-exists? input)]}
  (io/reader input))

(defn- parse-mapping
  "Parse Elasticsearch mapping from `path`."
  [path]
  {:pre [(file-exists? path)]}
  (-> path io/reader (json/parse-stream true)))

(defn- parse-jsonld
  "Parse `jsonld` to a vector to be used by Elasticsearch bulk loader."
  [jsonld]
  (let [parsed-jsonld (dissoc (json/parse-string jsonld) "@context")]
    [{"index" {"_id" (get parsed-jsonld "@id")}} (dissoc parsed-jsonld "@id")]))

(defn- main
  [{:keys [batch-size]
    :as config}
   {:keys [input mapping]
    :as options}]
  (let [settings (:settings mapping)
        [mapping-type mapping'] (first (:mappings mapping))
        mapping-type' (name mapping-type)]
    (mount/start-with-args (assoc config
                                  :mapping mapping'
                                  :mapping-type mapping-type'
                                  :settings settings))
    (with-open [input' (if-not (instance? BufferedReader input) (BufferedReader. input) input)]
      (doseq [documents (partition-all batch-size (map parse-jsonld (line-seq input')))]
        (esb/bulk-with-index-and-type (:conn elasticsearch)
                                      (:index elasticsearch)
                                      mapping-type'
                                      (apply concat documents))))))

; ----- Private vars -----

(def ^:private cli-options
  [["-c" "--config CONFIG" "Path to configuration file in EDN"
    :parse-fn parse-config
    :validate [validate-config "Invalid configuration"]]
   ["-i" "--input INPUT" "Line separated JSON-LD documents"
    :parse-fn parse-input
    :default *in*]
   ["-m" "--mapping MAPPING" "A path to Elasticsearch mapping of the loaded type"
    :parse-fn parse-mapping
    :validate [validate-mapping "Invalid mapping"
               (comp (partial = 1) count :mappings) "More than one mapping provided."]]
   ["-h" "--help" "Display help message"]])

; ----- Public functions -----

(defn -main
  [& args]
  (let [{{:keys [config help]
          :as options} :options
         :keys [errors summary]} (parse-opts args cli-options)
        ; Merge defaults
        config' (merge {:batch-size 5000} config)]
    (cond help (info (usage summary))
          errors (die (error-msg errors))
          :else (main config' options))))
