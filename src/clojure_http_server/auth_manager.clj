(ns clojure-http-server.auth-manager
  (:require [clojure.java.jdbc :as jdbc]
            [clojure-http-server.dal.db :refer [db-connection]]))

(defn get-token-user
  [auth-token]
  (let [[user] (jdbc/query (db-connection) ["SELECT users.id, users.fullname, users.username, users.email, users.profile_picture
                                            FROM sessions JOIN users ON
                                            sessions.user_id=users.id
                                            WHERE sessions.token=?
                                            LIMIT 1"
                                            auth-token])]
    (if-not user false user)))

(defn destroy-session
  [auth-token]
  (jdbc/delete! (db-connection) :sessions ["token = ?" auth-token]))

(defn create-session
  [user-id]
  (let [auth-token (crypto.random/url-part 64)]
    (jdbc/insert! (db-connection) :sessions {:token auth-token :user_id user-id})
    auth-token))

(defn auth-middleware
  [next]
  (fn [req]
    (let [token (or (get (:headers req) "auth-token") (get (:query-params req) "auth-token"))
          user (get-token-user token)]
      (if user
        (next (assoc req :auth-user user))
        {:status 401
         :body nil}))))