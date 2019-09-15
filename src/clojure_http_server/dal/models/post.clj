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

(defn get-all-posts
  []
  (jdbc/query (db-connection) "SELECT users.id as user_id,users.username,users.profile_picture, posts.*, GROUP_CONCAT(likes.user_id) as likes
                               FROM posts
                               JOIN users ON posts.user_id=users.id
                               LEFT JOIN likes ON posts.id = likes.post_id GROUP BY posts.id ORDER BY created_at DESC")
  )

(defn get-user-posts
  [user-id]
  (jdbc/query (db-connection) ["SELECT users.id as user_id,users.username,users.profile_picture, posts.*, GROUP_CONCAT(likes.user_id) as likes
                               FROM posts JOIN users ON posts.user_id=users.id
                               LEFT JOIN likes ON posts.id = likes.post_id WHERE posts.user_id = ?
                               GROUP BY posts.id
                               ORDER BY created_at DESC" user-id])
  )