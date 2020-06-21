(ns clojure-http-server.dal.models.comment
  (:require [clojure.java.jdbc :as jdbc]
            [clojure-http-server.dal.db :refer [db-connection]]))

(defn create
  "creates new comment"
  [comment]
  (let [[key] (jdbc/insert! (db-connection) :comments comment)]
    (:generated_key key)))