(ns clojure-http-server.dal.models.subscription
  (:require [clojure.java.jdbc :as jdbc]
            [clojure-http-server.dal.db :refer [db-connection]]))

(defn create
  "creates new subscription"
  [subscription]
  (jdbc/insert! (db-connection) :subscriptions subscription))

(defn get-subscribers-for-user
  [user-id]
  (let [results (jdbc/query (db-connection) ["SELECT * FROM subscriptions WHERE subscribing_to_user_id=?" user-id])]
    (map (fn [res] (:subscriber_user_id res)) results)))