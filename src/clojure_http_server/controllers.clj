(ns clojure-http-server.controllers
  (:require [clojure.string :as str]
            [clojure-http-server.dal.models.user :as User]
            [clojure-http-server.utils :refer [if-valid]]
            [crypto.password.scrypt :as password]
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