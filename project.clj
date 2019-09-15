(defproject clojure-http-server "0.1.0-SNAPSHOT"
  :description "REST Service for mobile application"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [
                 [org.clojure/clojure "1.10.0"]
                 ; Compojure - A basic routing library
                 [compojure "1.6.1"]
                 ; Our Http library for client/server
                 [http-kit "2.3.0"]
                 ; Ring defaults - for query params etc
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-json "0.5.0"]
                 [ring-cors "0.1.13"]
                 [org.clojure/java.jdbc "0.7.9"]
                 ;MySql driver
                 [mysql/mysql-connector-java "8.0.17"]
                 ;database connection-pooling lib
                 [com.mchange/c3p0 "0.9.5.2"]
                 [crypto-password "0.2.1"]
                 [crypto-random "1.2.0"]
                 ]
  :plugins [[lein-cljfmt "0.6.4"]]
  :repl-options {:init-ns clojure-http-server.core})
