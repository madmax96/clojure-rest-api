(ns clojure-http-server.core
  (:require [org.httpkit.server :as server]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer :all]
            )
  (:gen-class)
  )

(defroutes app-routes
  (GET "/test" [] #({:status 200})))

(defn -main
  "This is our main entry point"
  [& args]
  (let [port (Integer/parseInt (or (System/getenv "PORT") "3000"))]
    ; Run the server with middlewares
    (server/run-server (-> app-routes
                           (wrap-defaults api-defaults)
                           ) {:port port})
    (println (str "Running webserver at http:/127.0.0.1:" port "/")))
  )
