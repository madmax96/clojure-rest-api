(ns clojure-http-server.websocket-manager
  (:require [org.httpkit.server :as server]
            [clojure.data.json :as json]
            [clojure-http-server.dal.models.message :as Message]
            [clojure-http-server.utils :as Utils]))

(declare handle-new-message-event handle-message-seen-event)
(defonce events {:new-message "New Message"
                 :message-seen "Message Seen"
                 :new-post "New Post"
                 :new-comment "New Comment"
                 :post-like "Post Like"
                 :new-subscriber "New Subscriber"})

;keeping track of all connected ws-clients
;channels is a map where key is user-id and value is channel to communicate with that user
(defonce channels (atom {}))

(defn- handle-ws-message [message user]
  (let [event (get message "event") data (get message "data")]
    (cond (= event (:new-message events)) (handle-new-message-event data user)
          (= event (:message-seen events)) (handle-message-seen-event data user))))

(defn ws-handler [req]
  (let [user (:auth-user req) user-id (:id user)]
    (server/with-channel req channel ; get the channel
      (if (server/websocket? channel)
        (do
          (println "Creating new WebSocket client")
          (swap! channels conj [user-id channel])
          (println @channels)
          (server/on-receive channel (fn [message] ; message received from client
                                       (println "ws message received: " message)
                                       (handle-ws-message  (json/read-str message) user)))
          (server/on-close channel (fn [status]
                                     (do
                                       (println "channel closed")
                                       (swap! channels (fn [current-value] (dissoc current-value user-id)))
                                       (println @channels)))))))))

(defn emmit-user-subscription-event [subscription]
  "Sends notification to a user who gets new subscriber"
  (let [channel (get @channels (:subscribing_to_user_id subscription))]
    (if channel (server/send! channel (json/write-str {:event (:new-subscriber events) :data subscription})))))

(defn emmit-new-post-event
  [subscriber-ids created-post]
  "Sends New Post notification to all subscribed users"
  (doseq [user-id subscriber-ids]
    (let [channel (get @channels user-id)]
      (if channel (server/send! channel (json/write-str {:event (:new-post events) :data created-post}))))))

(defn emmit-post-like-event
  [liked-post liked-by-user]
  "Sends notification to a user who gets a like to his post"
  (let [channel (get @channels (:user_id liked-post))]
    (if channel (server/send! channel (json/write-str {:event (:post-like events) :data {:liked-post liked-post :liked-by-user liked-by-user}})))))

(defn emmit-new-comment-event
  [comment]
  "Sends New Comment notification to post creator"
  (let [channel (get @channels (:user_id (:post comment)))]
    (if channel (server/send! channel (json/write-str {:event (:new-comment events) :data comment})))))

(defn handle-new-message-event
  [{receiver-user-id "receiverUserId" type "type"  content "content"} user]
  (println "handling new message event")
  (let [message {:sender_user_id (:id user)
                 :receiver_user_id receiver-user-id
                 :content (if (= type "image")
                            (Utils/handle-base64Image-upload content (:username user)) ;if type of message is image, we store image filename in content field
                            content)
                 :type type}
        message-id (Message/create message)
        created-message (assoc message :id message-id)
        channel (get @channels receiver-user-id)]
    (if channel (server/send! channel (json/write-str {:event (:new-message events) :data created-message})))))

(defn handle-message-seen-event
  [{message-id "messageId" sender-user-id "senderUserId"} {user-id :id}]
  (Message/update-seen message-id user-id)
  (let [channel (get @channels sender-user-id)]
    (if channel (server/send! channel (json/write-str {:event (:message-seen events) :data {:message-id message-id :user-id user-id}})))))
