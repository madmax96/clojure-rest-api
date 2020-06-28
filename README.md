# REST API in Clojure
 This project represents simple REST api made with clojure and it's libs,
 primarily intended to be used by instagram-like, image sharing, social applications. 
 One such application, that actually uses this API can be found here: https://github.com/madmax96/ionic-image-sharing-app

 API supports basic user registration and  authentication, users can upload profile pictures,
 make image posts with description, delete their posts, view other users profiles and like other users posts,
 view some statistics about their posts, etc..

 API could be developed further to support for example commenting on posts, liking comments,
 real-time notifications (via websocket protocol) for some app events like for example when someone comments or likes user's post etc..

# Update - 30.06.2020
    Main update is that server now supports connections over websocket protocol. 
    Other than that, couple new endpoints are added:
        - Enpoint to create a comment
        - Endpoint to subscribe to some user
        - Enpoint to get all subscribers of a user
        - Endpoint to get all chat messages between two users
        
## Websocket support
In orded to connect to websocket server, client should send a websocket handshake request to "/ws" route.
In browsers we can do that by simply instantiating the WebSocket object: `new WebSocket(url)`

Route for websocket connection is protected with authentication middleware, so only users that are logged in can connect to it.

After connection is established, client and server can exchange messages in textual or binary format. 
This server support only text messages - JSON encoded strings with specific format.

Format of supported WebSocket message:
```
{
    "event": <string>,
    "data": <object>
}
```
Currently, server supports following events:
- New Message
- Message Seen
- New Post
- New Comment
- New Subscriber
- Post Like

## Notifications

 Server will emmit following notifications:
 
 - When user creates new post all of his subscribers(who are online) will get NewPost notification:
 ```
{
    "event": "New Post",
    "data": <object>   // Post Model
}
```

 - When user comments on some post, the author of that post gets the notification (if online)
 ```
{
    "event": "New Comment",
    "data": <object>   // Comment Model
}
```

 - When user gets new subscriber he will get notification (if online)
 ```
{
    "event": "New Subscriber",
    "data": <object>   // User Model
}
```
- When user gets new like on a post he will get notification (if online)
 ```
{
    "event": "Post Like",
    "data": {
                "liked-post": <object>, // Post Model
                "liked-by-user": <object> // User Model
            }
}
```
  
## Chat
There are two events that are used in chat - `New Message` and `Message Seen`

Message can be either text-message or image.

Textual message payload:
```
{
    "event": "New Message",
    "data": {
                "receiverUserId": <int>,
                "type": "text",
                "content": <string>
            }
}
```

Image message payload:
```
{
    "event": "New Message",
    "data": {
                "receiverUserId": <int>,
                "type": "image",
                "content": <Base64Image> // image encoded as Base64 string
            }
}
```
NOTE: When server receives image message it will store that image and replace "content" field 
with generated filename for that image, and only then server sends "New Message" event to other user.

Message Seen payload:
```
{
    "event": "Message Seen",
    "data": {
                "messageId": <int>,
                "senderUserId": <int>,
            }
}
```


# An example of app that uses this api

    Login Form
    
![](resources/description-images/user-login.jpeg?raw=true)

    Register Form
    
![](resources/description-images/user-register.jpeg?raw=true)

    Creating post
    
![](resources/description-images/create-post.jpeg?raw=true)

    User profile

![](resources/description-images/user-profile.jpeg?raw=true)

    Home Page
    
![](resources/description-images/home-page.jpeg?raw=true)    

    Post Stats
    
![](resources/description-images/stats.jpeg?raw=true)    

    Chat Tab - List With all users available for chat
![](resources/description-images/chat-list.png?raw=true)
    
    Chat Window - Message is sent but not yet seen by user
![](resources/description-images/chat-window-1.png?raw=true)

    Chat Window - Message is seen and user responds
![](resources/description-images/chat-window-2.png?raw=true)

    Chat Window - Sending images in chat
![](resources/description-images/chat-window-3.png?raw=true)
![](resources/description-images/chat-window-5.png?raw=true)
![](resources/description-images/chat-window-4.png?raw=true)

## License

Copyright © 2019 Simonovic Mladjan

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
