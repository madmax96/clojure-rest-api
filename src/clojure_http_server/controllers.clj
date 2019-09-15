(ns clojure-http-server.controllers
  (:require [clojure.string :as str]
            [clojure-http-server.dal.models.user :as User]
            [clojure-http-server.dal.models.post :as Post]
            [clojure-http-server.utils :refer [if-valid]]
            [crypto.password.scrypt :as password]
            [clojure-http-server.auth-manager :refer :all]
            [ring.util.codec :refer [base64-decode]]
            )
  (:import (java.sql SQLException)
           (java.io File)))

(defn create-user
  [req]
  (let [user-data (:body req)]
    (if-valid user-data  User/user-validations errors
        (try
          (let [inserted_id (User/insert (update user-data :password password/encrypt))]
            {
             :status 200
             :body  inserted_id
             }
            )
          (catch SQLException e
            {
             :status 400
             :body {:error (.getMessage e)}
             }
            )
          (catch Exception e
            {
             :status 500
             :body {:error "Unknown error"}
             }
            )
          )
        {
         :status 400
         :body {:validation-errors errors}
         }
      )
    )
  )

  (defn user-login
  [req]
  (let [data (:body req)
        email (:email data)
        password (:password data)
        user (User/find-by-email email)
        password-hash (:password user)
        ]
    (if (empty? user)
      {
       :status 404
       :body {:error (str  "User with email" email "does not exist. Please Sign Up")}
       }
      (if (password/check password password-hash)
        (let [auth-token
              (create-session (:id user))]
          {
           :status 200
           :headers {"auth-token" auth-token}
           :body user
           }
          )
        {
         :status 401
         :body {:error "Wrong password"}
         }
        )
      )
    )
  )

(defn get-session
  [req]
  {
   :status 200
   :body (:auth-user req)
   }
  )

(defn user-logout
  [req]
  (let [token (get (:headers req) "auth-token" ) ]
      (destroy-session token)
      {
       :status 200
       :body nil
       }
    )
  )

(defn check-username
  [req]
  (let [username (get (:query-params req) "username" )
        results (User/find-by-username username)]
    {
     :status 200
     :body {:available (= (:c (first results)) 0 )}
     }
    )
  )

(defn- handle-base64Image-upload
  [image username]
  (let [[info image-data] (str/split image #",")
        [image-info encoding] (str/split info #";")
        [image-type format] (str/split image-info #"/")]
    (if (or
          (not (= image-type "data:image"))
          (not (= encoding "base64"))
          )
      false
      (let [decoded (base64-decode image-data)
            filename (str username "-" (crypto.random/url-part 8) "." format)
            ]
        (clojure.java.io/copy
          decoded
          (File. (str "resources/image-uploads/" filename)))
        filename
        )
      )
    )
  )
(defn update-user
  [req]
  (let [user (:auth-user req)
        data (:body req)
        image (:base64Image data)
        uploaded-image-filename (handle-base64Image-upload image (:username user))
        ]
    (if (not uploaded-image-filename)
      {
       :status 400
       :body {:error "Only base64 encoded images are allowed"}
       }
      (do
        ;Currently allowing only update of profile picture
        (User/update-user (:id user) {:profile_picture uploaded-image-filename})
        {
         :status 200
         :body (assoc user :profile_picture uploaded-image-filename)
         }
        )
      )
    )
  )

(defn create-post
  [req]
  (let [user (:auth-user req) {description :description image :base64Image} (:body req)]
    (if (or
          (not description)
          (not image))
      {
       :status 400
       :body {:error "Description and base64Encoded image must be sent"}
       }
      (let [uploaded-image-filename (handle-base64Image-upload image (:username user))
            post {
                  :description description
                  :image_filename uploaded-image-filename
                  :user_id (:id user)}
            post-id (Post/create post)
            ]
        {
         :status 200
         :body (assoc post :id post-id)
         }
        )
      )
    )
  )

(defn get-posts
  [req]
  (let [user-id (:user-id (:params req))
        posts (if user-id
                (Post/get-user-posts user-id)
                (Post/get-all-posts)
                )]
    {
     :status 200
     :body posts
     }
    )
  )