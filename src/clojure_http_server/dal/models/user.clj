(ns clojure-http-server.dal.models.user
  (:require [clojure.java.jdbc :as jdbc]
            [clojure-http-server.dal.db :refer [db-connection]]))

(def user-validations
  {:fullname
   ["Please enter your full name" not-empty
    "Name should contain only letters" #(re-find #"^[a-zA-Z][a-zA-Z\s]*$" %)]
   :username
   ["Username should be at least 3 characters long" #(< 2 (count %))
    "Username should contain only letters" #(re-find #"^[a-z\-]+$" %)]
   :email
   ["Please enter an email address" not-empty
    "Your email address is not valid"
    #(or (empty? %) (re-seq #"@" %))]
   :password
   ["Password should be at least 6 characters long" #(< 5 (count %))]})

(defn find-by-username
  "Search users table with provided params"
  [username]
  (jdbc/query (db-connection) ["SELECT COUNT(*) as c FROM users WHERE username=?" username]))

(defn get-by-id
  [id]
  (let [[user] (jdbc/query (db-connection) ["SELECT id,fullname,email,username,profile_picture FROM users WHERE id=?" id])]
    user))

(defn find-by-email
  [email]
  (let [[user] (jdbc/query (db-connection) ["SELECT * FROM users WHERE email=?" email])]
    user))

(defn insert
  "insert new user"
  [user]
  (let [[key] (jdbc/insert! (db-connection) :users user)]
    {"user_id" (:generated_key key)}))

(defn update-user
  [user-id update-map]
  (jdbc/update! (db-connection) :users update-map ["id = ?" user-id]))
