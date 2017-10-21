(ns karriboard.setupengine
    (:require [karriboard.SQL :as sql]
              [clojure.java.jdbc :as j]))

(defn setup
    "Создание таблиц."
    []
    (j/db-do-commands sql/db
        (j/create-table-ddl :ID [[:ID "bigint"]]))
    (j/insert! sql/db :ID {:ID 0})
    (j/db-do-commands sql/db
        (j/create-table-ddl :anons
                        [[:ID "bigint"]
                         [:Anons_text "varchar(6000)"]
                         [:Anons_name "varchar(50)"]
                         [:Board "varchar(15)"]
                         [:Thread_ID "bigint"]
                         [:User_agent "varchar(500)"]
                         [:IP "varchar(30)"]
                         [:Password "varchar(50)"]]))
    (j/db-do-commands sql/db
        (j/create-table-ddl :boards
            [[:Name "varchar(100)"]
             [:Char "varchar(20)"]]))
    (let [x (vector "Random" "b" "Technology" "g" "Anime & Manga" "a" "Karriboard" "kb")]
     (loop [y 0]
      (when (> (count x) y)
       (j/db-do-commands sql/db
           (println (format "Created board: %s" (get x (inc y))))
           (j/create-table-ddl (keyword (get x (inc y)))
            [[:ID "bigint"]
             [:Thread_name "varchar(144)"]
             [:Thread_text "varchar(6000)"]
             [:Anons_name "varchar(50)"]
             [:User_agent "varchar(500)"]
             [:IP "varchar(30)"]
             [:Password "varchar(50)"]]))
       (j/insert! sql/db :boards {:Name (get x y) :Char (get x (inc y))})
       (recur (+ y 2))))))
