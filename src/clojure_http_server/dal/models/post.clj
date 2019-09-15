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

(defn delete
  [post-id user-id]
  (jdbc/delete! (db-connection) :posts ["id=? AND user_id=?" post-id user-id])
  )

(defn get-stats
  [user-id]
  (let [[stats]
        (jdbc/query (db-connection) ["SELECT SUM(total_likes) as total_likes,
                                      COUNT(post_id) as total_posts
                                      FROM (SELECT posts.id as post_id , COUNT(likes.post_id) as total_likes
                                            FROM posts LEFT JOIN likes ON posts.id=likes.post_id
                                            WHERE posts.user_id = ?
                                            GROUP BY posts.id) as T
                                      " user-id])
        total-posts (:total_posts stats)
        ]

        (if (= total-posts 0)
          (conj stats
                {:avg_likes 0 :total_likes 0})
          (conj stats
                {:avg_likes (with-precision 3 (/ (:total_likes stats) total-posts))})
          )
        )
  )