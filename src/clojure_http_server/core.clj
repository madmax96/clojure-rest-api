(ns clojure-http-server.core
  (:require [org.httpkit.server :as server]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer :all]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [clojure-http-server.controllers :as Controller]
            [clojure-http-server.auth-manager :refer [auth-middleware]]
            [clojure-http-server.websocket-manager :as WS-Manager])
  (:gen-class))

(defroutes app-routes
  (GET "/ws" [] (auth-middleware WS-Manager/ws-handler))
  (POST "/user" [] Controller/create-user)
  (POST "/subscriptions/:user-id" [] (auth-middleware Controller/subscribe-to-user))
  (GET "/user/check-username" [] Controller/check-username)
  (PATCH "/user" [] (auth-middleware Controller/update-user))
  (POST "/session" [] Controller/user-login)
  (DELETE "/session" [] (auth-middleware Controller/user-logout))
  (GET "/session" [] (auth-middleware Controller/get-session))
  (POST "/posts" []  (auth-middleware Controller/create-post))
  (GET "/posts" []  (auth-middleware Controller/get-posts))
  (GET "/posts/:user-id" []  (auth-middleware Controller/get-posts))
  (DELETE "/posts/:post-id" []  (auth-middleware Controller/delete-post))
  (GET "/stats/posts" [] (auth-middleware Controller/get-posts-stats))
  (POST "/posts/:post-id/comment" [] (auth-middleware Controller/create-comment))
  (POST "/likes/:post-id" []  (auth-middleware Controller/like-post))
  (GET "/messages/:user-id" [] (auth-middleware Controller/get-users-chat))
  (auth-middleware (route/resources "/images" {:root "image-uploads"}))
  (route/not-found {:err "Route not found"}))

(defn -main
  "This is our main entry point"
  [& args]
  (let [port (Integer/parseInt (or (System/getenv "PORT") "3000"))]
    ; Run the server with middlewares
    (server/run-server (-> app-routes
                           (wrap-cors :access-control-allow-origin [#"http://localhost:4200"]
                                      :access-control-allow-methods [:get :put :post :delete :options :patch]
                                      :access-control-expose-headers ["Auth-Token"])
                           wrap-json-response
                           (wrap-json-body {:keywords? true :bigdecimals? true})
                           (wrap-defaults api-defaults)) {:port port})
    (println (str "Running webserver at http://localhost:" port "/"))))
