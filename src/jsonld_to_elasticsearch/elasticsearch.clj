(ns jsonld-to-elasticsearch.elasticsearch
  (:require [clojurewerkz.elastisch.rest :as es]
            [clojurewerkz.elastisch.rest.index :as esi]
            [clj-http.client :as client]
            [mount.core :as mount :refer [defstate]]))
(defn init
  [{:keys [endpoint index mapping mapping-type settings]
    :as config}]
  (let [conn (es/connect endpoint)]
    ; Create index if not exists.
    (when-not (esi/exists? conn index)
      (client/put (str endpoint "/" index)))
    ; Add settings if provided
    (when settings
      (esi/close conn index)
      (esi/update-settings conn index settings)
      (esi/open conn index))
    ; Add type mapping
    (when-not (esi/type-exists? conn index mapping-type)
      (esi/update-mapping conn index mapping-type {:mapping mapping}))
    (assoc config :conn conn)))

(defstate elasticsearch
  :start (init (mount/args)))
