(ns clojure-http-server.core
  (:require [org.httpkit.server :as server]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer :all]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [clojure-http-server.controllers :as Controller]
            [clojure-http-server.auth-manager :refer [auth-middleware]])
  (:gen-class))

;keeping track of all connected ws-clients
(defonce channels (atom #{}))

(defn ws-handler [req]
  (server/with-channel req channel              ; get the channel
                ;; communicate with client using method defined above
    (server/on-close channel (fn [status]
                               (println "channel closed")))
    (if (server/websocket? channel)
      (do
        (println "WebSocket channel")
        (swap! channels conj channel)
        (server/on-receive channel (fn [data]
                                    ; data received from client
                                     (println "data received: " data)
                                    ;; An optional param can pass to send!: close-after-send?
                                    ;; When unspecified, `close-after-send?` defaults to true for HTTP channels
                                    ;; and false for WebSocket.  (send! channel data close-after-send?)
                                     (server/send! channel (str "Response" data)))))))) ; data is sent directly to the client

(defroutes app-routes
  (GET "/ws" [] ws-handler)
  (POST "/user" [] Controller/create-user)
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
