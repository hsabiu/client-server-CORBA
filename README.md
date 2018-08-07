## Introduction
This is a client-server chat application implemented using Java and CORBA.

### Compiling and Testing
From the project root directory, open the terminal and run the following comands:
    
    mkdir bin
    idlj -td src/ -fall MyInterfaces.idl
    javac src/*.java src/ChatApp/*.java -d bin/
    orbd -ORBInitialPort 1050 -ORBInitialHost localhost

Start the server by running the following command:

    java -cp bin/ CORBAServer -ORBInitialPort 1050 -ORBInitialHost localhost

After the server started successfully, create multiple clients using the following command:

    java -cp bin/ CORBAClient -ORBInitialPort 1050 -ORBInitialHost localhost

Before a client can connect to the web server, the client has to provide a user name to the server. The server then checks to make sure no any other connected client is using the  same user name. If the user name exist on the server, an error message is displayed asking the user to choose a different name. Otherwise the server register the user to the default 'general' room and inform all the users of this room that a new client is connected.

Use the created clients to interact with the server by performing various operations such as:

  * Create chat-rooms by issuing the command **'/create roomName'**
  * List all existing rooms by issuing the command **'/rooms'**
  * Join existing chat-rooms by issuing the command **'/join roomName'**
  * Send messages to chat-rooms by simply writing a message into the client text box and pressing the enter key or send button on the client
  * Leave a chat-room by issuing the command **'/leave roomName'**
  * List all users by issuing the command **'/users'**

When a user connect to a new room, all the previous messages in this room are displayed to the user. In addition, all messages sent by the user are shown to all other clients connected to the same room with 1 second maximum delay. When a user leave a room, the server disconnect the user from the system.
