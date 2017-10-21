(ns karriboard.parser
    (:require [clojure.data.json :as json]))

(defn get_dbuser
    []
    (let [data (slurp "config.json")]
        (get (json/read-str data) "DB_USER")))

(defn get_dbpass
    []
    (let [data (slurp "config.json")]
    (get (json/read-str data) "DB_PASSWORD")))
