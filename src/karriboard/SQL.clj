(ns karriboard.SQL
 (:require
     [clojure.java.jdbc :as j]))

(defn- create-db-info
    "Создание мапы с инфой."
    ;[(and (let [x (class user)](if (= (str x) "class java.lang.String") true false))(let [x (class password)](if (= (str x) "class java.lang.String") true false)))
    ^{:pre [(and (string? user) (string? password))]}
    [user password]
    (def db
        {:dbtype "postgresql" :dbname "karriboard" :user user
            :password password :host "localhost"}))

(defn db-initialize
    [username password]
    (create-db-info username password)
    (println (str "-----\nBDType: "(get db :dbtype) "\nHost: " (get db :host) "\nBD: " (get db :dbname) "\nUser: " (get db :user) "\n-----\nOK")))

(defn execute
        [query]
        (j/query db [query]))

(defn last-id
    []
    (get (first (j/query db ["select * from id"])) :id))

(defn board
    "Список борд"
    [index]
    (let [x (vec (j/query db ["select * from boards"]))]
     (if (< index (count x))
      (get-in x [index :char])
      false)))

(defn create-thread
    "Создать нить."
    [board_char thread_name thread_text anons_name user_agent IP password]
    (let [ID (last-id)]
        (println (str "ID:" ID "TN:" thread_name "TT:" thread_text "AN:" anons_name "user_agent:" user_agent "IP:" IP "PASS:" password))
        (loop [c 0]
            (let [v (board c)]
             (if (not= false v)
              (if (= v board_char)
                (when-let [xyz (str "select count(*) from " v)]
                 (>= ID 0)
                 ((fn [] (j/update! db :ID {:ID (inc ID)} ["id= ?" ID])
                      (j/insert! db (keyword v) {:ID (inc ID) :thread_name thread_name
                                                     :thread_text thread_text :anons_name anons_name :user_agent user_agent
                                                     :IP IP :password password})
                      "{'status':'200'}")))
                (recur (inc c)))
              "{'status':'404', 'error_code': '0', 'error_msg': 'Борда не найдена'}")))))

(defn create-answer
    "Написать ответ."
    [board_char anons_name anons_text thread_id user_agent IP password]
    (println board_char)
    (let [ID (last-id)]
     (loop [c 0]
         (let [v (board c)]
          (if (not= false v)
           (if (= v board_char)
            ((fn [] (j/update! db :ID {:ID (inc ID)} ["id= ?" ID])
              (let [zyx (str "select count(*) from " board_char " where id=" thread_id)]
               (if (= (:count (first (j/query db [zyx]))) 1)
                ((fn [] (j/insert! db :anons {:ID (inc ID) :anons_text anons_text :anons_name anons_name
                                              :thread_id (Integer/parseInt thread_id) :user_agent user_agent
                                              :IP IP :password password :board board_char})
                        "{'status':'200'}"))
                "{'status':'404', 'error_code': '1', 'error_msg': 'Тред не найден'"))))
            (recur (inc c)))
           "{'status':'404', 'error_code': '0', 'error_msg': 'Борда не найдена'}")))))
