(ns clojure-http-server.websocket-manager
  (:require [org.httpkit.server :as server]
            [clojure.data.json :as json]))

(defonce events {:message-sent "Message Sent"
                 :message-seen "Message Seen"})

;keeping track of all connected ws-clients
;channels is a map where key is user-id and value is channel to communicate with that user
(defonce channels (atom {}))

(defn- handle-ws-message [data user channel]
  (server/send! channel (json/write-str (assoc data :status "OK"))))

(defn ws-handler [req]
  (let [user (:auth-user req) user-id (:id user)]
    (server/with-channel req channel ; get the channel
                         (if (server/websocket? channel)
                           (do
                             (println "Creating new WebSocket client")
                             (swap! channels conj [user-id channel])
                             (println @channels)
                             (server/on-receive channel (fn [data]
                                                          ; data received from client
                                                          (println "data received: " data)
                                                          (handle-ws-message  (json/read-str data) user channel)))
                             (server/on-close channel (fn [status]
                                                        (do
                                                          (println "channel closed")
                                                          (swap! channels (fn [current-value] (dissoc current-value user-id)))
                                                          (println @channels)))))))))