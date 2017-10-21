(ns karriboard.core
    (:gen-class)
    (:require
        [ring.adapter.jetty :only (run-jetty) :as jetty]
        [compojure.core :refer :all]
        [compojure.route :as route]
        [hiccup.core :as hc]
        [clojure.string :as str]
        [compojure.handler :as handler]
        [clojure.core.async :as async]
        [clojure.java.jdbc :as j]
        [clojure.data.json :as json]
        [ring.util.response :as resp]
        [karriboard.parser :as parser]
        [karriboard.setupengine :as set]
        [karriboard.SQL :as sql]))

(def ans (async/chan 1))
(def thr (async/chan 1))
(def resp0 (async/chan 1))
(def resp1 (async/chan 1))

(defn route-create-answer [{:keys [params] ip :remote-addr usag :headers}]
    (async/>!! ans (conj {:ip ip :user_agent usag} params))
    (async/<!! resp0))

(defn route-create-thread [{:keys [params] ip :remote-addru usag :headers}]
    (async/>!! thr (conj {:ip ip :user_agent usag} params))
    (async/<!! resp1))

(defn get-content-threads ;board
        [{:keys [params]}]
        (println (str "select * from " (get params :board)))
        (loop [x 0]
          (let [y (sql/board x)]
           (if (not= y false)
            (if (= (get params :board) y)
                ((fn []
                    (json/write-str (sql/execute (str "select * from " (get params :board))))))
                (recur (inc x)))
            "{'status':404}"))))

(defn get-content-answers ;board&thread_id
    [{:keys [params]}]
    (println (get params :board))
    (let  [query (str "select * from anons where board='" (get params :board) "' and thread_id=" (get params :thread_id))]
        (loop [x 0]
            (let [y (sql/board x)]
                (if (= (get params :board) y)
                    (json/write-str (sql/execute query))
                    "{'status':404}")))))

(defroutes core-routes
    (GET "/" [] (resp/file-response "index.html" {:root "public"}))
    (POST "/core/v1/create_thread" [] route-create-thread)
    (POST "/core/v1/create_answer" [] route-create-answer)
    (GET "/core/v1/get_content_threads" [] get-content-threads)
    (GET "/core/v1/get_content_answers" [] get-content-answers)
    (route/not-found "Not Found"))

(def core (handler/api core-routes))

(defn run-core [] (def server (jetty/run-jetty #'core {:port 8080 :async? false :max-threads 0})))

(defn chan-handlers []
    ((fn []
      (async/go-loop []
          (let [params (async/<!! ans)]
             (async/>!! resp0
                {:status 200 :headers {"Content-Type" "application/json; charset=utf-8"} :body (format "%s" (sql/create-answer (get params :board) (get params :anons_name) (get params :anons_text) (get params :thread_id) (str/replace (nth (str/split (str (get params :user_agent)) #" ") 1) #"," "") (str (get params :ip)) (get params :pass)))}))
       (recur))))
    ((fn []
        (async/go-loop []
              (let [params (async/<!! thr)]
               (async/>!! resp1
                {:status 200 :headers {"Content-Type" "application/json; charset=utf-8"} :body (format "%s" (sql/create-thread (get params :board) (get params :thread_name) (get params :thread_text) (get params :anons_name) (str/replace (nth (str/split (str (get params :user_agent)) #" ") 1) #"," "") (str (get params :ip)) (get params :pass)))}))
           (recur)))))

(defn -main
    [& args]
 (sql/db-initialize (parser/get_dbuser) (parser/get_dbpass))
 (when (= (get (first (sql/execute "select count(*) from information_schema.tables where table_schema = 'public'")) :count) 0)
  (set/setup))
 ((fn []
   (chan-handlers)
   (run-core))))
