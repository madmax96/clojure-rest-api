(ns clojure-http-server.dal.db
  (:import (com.mchange.v2.c3p0 ComboPooledDataSource))
  (:gen-class))

(def db-spec
  {:classname "com.mysql.cj.jdbc.Driver"
   :subprotocol "mysql"
   :subname "//127.0.0.1:3306/clojure_db"
   :user "madmax"
   :password "madmax"})

(defn- pool
  [spec]
  (let [cpds (doto (ComboPooledDataSource.)
               (.setDriverClass (:classname spec))
               (.setJdbcUrl (str "jdbc:" (:subprotocol spec) ":" (:subname spec)))
               (.setUser (:user spec))

               (.setPassword (:password spec))
               ; expire excess connections after 30 minutes of inactivity:
               (.setMaxIdleTimeExcessConnections (* 30 60))
               ; expire connections after 3 hours of inactivity:
               (.setMaxIdleTime (* 3 60 60)))]
    {:datasource cpds}))

(def pooled-db (delay (pool db-spec)))
(defn db-connection [] @pooled-db)

;init connection
(db-connection)

