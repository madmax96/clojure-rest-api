(ns clojure-http-server.controllers
  (:require [clojure.string :as str]
            [clojure-http-server.dal.models.user :as User]
            [clojure-http-server.utils :refer [if-valid]]
            [crypto.password.scrypt :as password]
            [clojure-http-server.auth-manager :refer :all]
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