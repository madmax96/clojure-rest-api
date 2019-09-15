(ns clojure-http-server.dal.models.post
  (:require [clojure.java.jdbc :as jdbc]
            [clojure-http-server.dal.db :refer [db-connection]])
  )

(defn create
  [post]
  (let [ res (jdbc/insert! (db-connection) :posts post)]
    (:generated_key (first res))
    )
  )