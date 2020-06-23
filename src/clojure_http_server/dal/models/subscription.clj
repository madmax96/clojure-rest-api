(ns clojure-http-server.dal.models.subscription
  (:require [clojure.java.jdbc :as jdbc]
            [clojure-http-server.dal.db :refer [db-connection]]))

(defn create
  "creates new subscription"
  [subscription]
  (jdbc/insert! (db-connection) :subscriptions subscription))