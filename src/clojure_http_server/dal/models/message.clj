(ns clojure-http-server.dal.models.message
  (:require [clojure.java.jdbc :as jdbc]
            [clojure-http-server.dal.db :refer [db-connection]]))

(defn create
  "creates new message"
  [message]
  (let [[res] (jdbc/insert! (db-connection) :messages message)]
    (:generated_key res)))

(defn get-users-chat
  [first-user-id second-user-id]
  (jdbc/query (db-connection) ["SELECT * FROM messages WHERE sender_user_id IN (?,?) AND receiver_user_id IN (?,?) ORDER BY created_at ASC"
                               first-user-id second-user-id first-user-id second-user-id]))

(defn update-seen
  "sets 'seen' property to true"
  [message-id user-id]
  (jdbc/update! (db-connection) :messages {:seen 1} ["id = ? AND receiver_user_id=?" message-id user-id]))