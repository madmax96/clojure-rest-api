(ns clojure-http-server.dal.models.subscription
  (:require [clojure.java.jdbc :as jdbc]
            [clojure-http-server.dal.db :refer [db-connection]]))

(defn create
  "creates new subscription"
  [subscription]
  (jdbc/insert! (db-connection) :subscriptions subscription))

(defn get-subscribers-for-user
  [user-id]
  (jdbc/query (db-connection)
              ["SELECT users.id,users.username,users.fullname,users.email,users.profile_picture
                            FROM subscriptions JOIN users ON users.id=subscriptions.subscriber_user_id
                            WHERE subscribing_to_user_id=?" user-id]))