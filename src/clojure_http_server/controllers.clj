(ns clojure-http-server.controllers
  (:require [clojure.string :as str]
            [clojure-http-server.dal.models.user :as User]
            [clojure-http-server.dal.models.post :as Post]
            [clojure-http-server.dal.models.like :as Like]
            [clojure-http-server.dal.models.comment :as Comment]
            [clojure-http-server.dal.models.subscription :as Subscription]
            [clojure-http-server.dal.models.message :as Message]
            [clojure-http-server.websocket-manager :as WS-Manager]
            [clojure-http-server.utils :as Utils]
            [crypto.password.scrypt :as password]
            [clojure-http-server.auth-manager :refer :all]
            [ring.util.codec :refer [base64-decode]])
  (:import (java.sql SQLException)
           (java.io File)))

(defn create-user
  [req]
  (let [user-data (:body req)]
    (Utils/if-valid user-data  User/user-validations errors
                    (try
                      (let [inserted_id (User/insert (update user-data :password password/encrypt))]
                        {:status 200
                         :body  inserted_id})

                      (catch SQLException e
                        {:status 400
                         :body {:error (.getMessage e)}})

                      (catch Exception e
                        {:status 500
                         :body {:error "Unknown error"}}))

                    {:status 400
                     :body {:validation-errors errors}})))

(defn subscribe-to-user
  [req]
  (let [user (:auth-user req)
        subscribing-to-user-id (Integer/parseInt (:user-id (:params req)))]
    (let [subscription {:subscriber_user_id (:id user) :subscribing_to_user_id subscribing-to-user-id}]
      (Subscription/create subscription)
      (WS-Manager/emmit-user-subscription-event subscription)
      {:status 200
       :body subscription})))

(defn user-login
  [req]
  (let [data (:body req)
        email (:email data)
        password (:password data)
        user (User/find-by-email email)
        password-hash (:password user)]
    (if (empty? user)
      {:status 404
       :body {:error (str  "User with email" email "does not exist. Please Sign Up")}}
      (if (password/check password password-hash)
        (let [auth-token
              (create-session (:id user))]
          {:status 200
           :headers {"auth-token" auth-token}
           :body user})

        {:status 401
         :body {:error "Wrong password"}}))))

(defn get-session
  [req]
  {:status 200
   :body (:auth-user req)})

(defn user-logout
  [req]
  (let [token (get (:headers req) "auth-token")]
    (destroy-session token)
    {:status 200
     :body nil}))

(defn check-username
  [req]
  (let [username (get (:query-params req) "username")
        results (User/find-by-username username)]
    {:status 200
     :body {:available (zero? (:c (first results)))}}))

(defn update-user
  [req]
  (let [user (:auth-user req)
        data (:body req)
        image (:base64Image data)
        uploaded-image-filename (Utils/handle-base64Image-upload image (:username user))]
    (if-not uploaded-image-filename {:status 400, :body {:error "Only base64 encoded images are allowed"}}
            (do (User/update-user (:id user) {:profile_picture uploaded-image-filename})
                {:status 200, :body (assoc user :profile_picture uploaded-image-filename)}))))

(defn create-post
  [req]
  (let [user (:auth-user req) {description :description image :base64Image} (:body req)]
    (if (or
         (not description)
         (not image))
      {:status 400
       :body {:error "Description and base64Encoded image must be sent"}}
      (let [uploaded-image-filename (Utils/handle-base64Image-upload image (:username user))
            user-id  (:id user)
            post {:description description
                  :image_filename uploaded-image-filename
                  :user_id user-id}
            post-id (Post/create post)
            created-post (assoc post :id post-id)
            subscribers (Subscription/get-subscribers-for-user user-id)
            subscriber-ids (map (fn [user] (:id user)) subscribers)]
        (WS-Manager/emmit-new-post-event subscriber-ids created-post)
        {:status 200
         :body created-post}))))

(defn create-comment
  [req]
  (let [user (:auth-user req)
        {text :text} (:body req)
        post-id (Integer/parseInt (:post-id (:params req)))]
    (if (not text)
      {:status 400
       :body {:error "Comment's text must be sent"}}
      (let [comment {:post_id post-id :user_id (:id user) :text text}
            comment-id (Comment/create comment)
            post (Post/get-by-id post-id)]
        (WS-Manager/emmit-new-comment-event {:post post :user user :text text})
        {:status 200
         :body (assoc comment :id comment-id)}))))

(defn get-posts
  [req]
  (let [user-id (:user-id (:params req))
        posts (if user-id
                (Post/get-user-posts user-id)
                (Post/get-all-posts))]
    {:status 200
     :body posts}))

(defn delete-post
  [req]
  (let [post-id (:post-id (:params req))
        user (:auth-user req)]
    (Post/delete post-id (:id user))
    {:status 200
     :body nil}))

(defn get-posts-stats
  [req]
  (let [user (:auth-user req)
        stats (Post/get-stats (:id user))]
    {:status 200
     :body stats}))

(defn like-post [req]
  (let [post-id (Integer/parseInt (:post-id (:params req)))
        user (:auth-user req)
        liked-post (Post/get-by-id post-id)]
    (Like/create {:post_id post-id :user_id (:id user)})
    (WS-Manager/emmit-post-like-event liked-post user))
  {:status 200
   :body nil})

(defn get-users-chat [req]
  (let [user-id (:user-id (:params req))
        auth-user (:auth-user req)
        messages (Message/get-users-chat user-id (:id auth-user))]
    {:status 200
     :body messages}))

(defn get-subscribers-for-user [req]
  (let [auth-user (:auth-user req)
        subscribers (Subscription/get-subscribers-for-user (:id auth-user))]
    {:status 200
     :body subscribers}))
