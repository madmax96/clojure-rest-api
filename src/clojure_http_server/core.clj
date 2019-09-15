(ns clojure-http-server.core
  (:require [org.httpkit.server :as server]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer :all]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [clojure-http-server.controllers :as Controller]
            )
  (:gen-class)
  )

(defroutes app-routes
  (POST "/user" [] Controller/create-user))

(defn -main
  "This is our main entry point"
  [& args]
  (let [port (Integer/parseInt (or (System/getenv "PORT") "3000"))]
    ; Run the server with middlewares
    (server/run-server (-> app-routes
                           (wrap-cors :access-control-allow-origin [#"http://localhost:8100"]
                                      :access-control-allow-methods [:get :put :post :delete :options :patch]
                                      :access-control-expose-headers ["Auth-Token"])
                           wrap-json-response
                           (wrap-json-body {:keywords? true :bigdecimals? true})
                           (wrap-defaults api-defaults)
                           ) {:port port})
    (println (str "Running webserver at http:/127.0.0.1:" port "/")))
  )
