(ns clojure-http-server.dal.models.like
  (:require [clojure.java.jdbc :as jdbc]
            [clojure-http-server.dal.db :refer [db-connection]]))

(defn create
  [like-map]
  (jdbc/insert! (db-connection) :likes like-map))
