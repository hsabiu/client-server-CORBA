//AUTHOR: HABIB ADO SABIU

import ChatApp.*;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CORBA.*;
import java.util.Scanner;
import java.io.*;
import java.util.regex.Pattern;

public class CORBAClient {

    static ServerInterface serverInterfaceImpl;

    public static String lastMessage = "";
    public static String userName = "";
    public static String connectedRoom = "";
    public static String strResponse = "";

    public static void main(String args[]) {

        try {
            // create and initialize the ORB
            ORB orb = ORB.init(args, null);

            // get the root naming context
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");

            // Use NamingContextExt instead of NamingContext. This is part of the Interoperable naming Service.  
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            // resolve the Object Reference in Naming
            String name = "ServerInterface";
            serverInterfaceImpl = ServerInterfaceHelper.narrow(ncRef.resolve_str(name));

            // print a message to the terminal
            System.out.println("Obtained a handle on server object: " + serverInterfaceImpl);

            // create BufferedReader 'br' to store input stream
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            // print to the terminal
            System.out.println("");
            System.out.print("Enter a name : ");
            String nameInput = br.readLine();
            System.out.println("");

            // get the response from the web server
            // if the response is 'failure'
            //    print a message saying the user needs to choose a different name
            // else, set the 'connectedRoom' to the default 'general'
            //    set the 'userName' to 'nameInput'
            //    print the 'connectedTime' to the terminal
            String connectionResponse = serverInterfaceImpl.connection(nameInput);
            if (connectionResponse.equals("failure")) {
                System.out.println("Please choose a different name");
            } else {
                connectedRoom = "general";
                strResponse = connectionResponse;
                userName = nameInput;

                String TimeStamp = new java.util.Date().toString();
                String connectedTime = "Connected on " + TimeStamp;

                System.out.println(connectedTime);
                System.out.println("");

                // get all the previously sent messages to this room and print it to the 
                // terminal
                String[] strResponseParts = strResponse.split(Pattern.quote("|"));
                for (int i = 1; i < strResponseParts.length - 1; i++) {
                    String[] strArr = strResponseParts[i].split(" ", 2);
                    System.out.println(strArr[1] + "\n");
                }

                // create a thread that will keep asking the server if there is a new message 
                // ment for the connected room every 500 miliseconds
                Thread receivingMessages = new Thread(new Runnable() {
                    public void run() {
                        while (true) {
                            String serverResponse = serverInterfaceImpl.getMessages(connectedRoom);
                            if (!(serverResponse.equals(lastMessage)) && !(serverResponse.equals("")) && !(serverResponse.startsWith("@" + userName))) {
                                lastMessage = serverResponse;
                                System.out.println(serverResponse + "\n");
                            }

                            try {
                                Thread.sleep(500);
                            } catch (Exception e) {
                                System.out.println(e);
                            }
                        }
                    }
                });

                // start the receiving messages thread
                receivingMessages.start();

                while (true) {

                    String input = br.readLine();
                    System.out.println("");

                    // if the 'input' starts with 'exit', disconnect the client by calling the remote method 'disconnect'
                    if (input.equals("exit")) {
                        serverInterfaceImpl.disconnect(userName, connectedRoom);
                        System.exit(0);


                    // if 'input' starts with the keyword '/join'
                    //    get the room name to join
                    //    if the user is already connected to that room, display a message indicating this
                    //    else, call the remote method 'joinRoom' and pass the room name to join and the 'userName'
                    //         get the respose from the remote method
                    //         if the response is 'joined', leave the previously connected method
                    //             display all the previous messages sent to the new connected room
                    //         else if the response is 'no-room', display room not found error
                    //         else, display invalid command error

                    } else if (input.startsWith("/join")) {

                        String[] inputParts = input.split(Pattern.quote(" "));
                        if (inputParts[1].equalsIgnoreCase(connectedRoom)) {
                            System.out.println("You are already in this room\n");
                        } else {
                            String response = serverInterfaceImpl.joinRoom(inputParts[1], userName);
                            String[] responseParts = response.split(Pattern.quote("|"));

                            if (response.startsWith("joined")) {

                                String leave = serverInterfaceImpl.leaveRoom(connectedRoom, userName);

                                connectedRoom = inputParts[1];
                                System.out.println("You have left " + connectedRoom + " room\n");

                                for (int i = 1; i < responseParts.length - 1; i++) {
                                    String[] arr = responseParts[i].split(" ", 2);
                                    System.out.println(arr[1] + "\n");
                                }

                            } else if (response.equals("no-room")) {
                                System.out.println(inputParts[1] + " room not found\n");
                            } else {
                                System.out.println("Invalid command room\n");
                            }
                        }

                    // else if the 'input' starts with the keyword '/leave'
                    //      first check the room to leave 
                    //      if it's general then display an error message
                    //      else check to make sure the room exist and the user is connected to the room
                    //      display the appropriate message (either success or failure)

                    } else if (input.startsWith("/leave")) {
                        String[] inputParts = input.split(Pattern.quote(" "));
                        if (inputParts[1].equals("general")) {
                            System.out.println("You cant leave general room\n");
                        } else {
                            String response = serverInterfaceImpl.leaveRoom(inputParts[1], userName);
                            if (response.equals("no-room")) {
                                System.out.println(inputParts[1] + " room not found\n");
                            } else if (response.equals("no-user")) {
                                System.out.println("You are not in " + inputParts[1] + " room\n");
                            } else if (response.equals("leave-success")) {
                                connectedRoom = "general";
                                System.out.println("You have left " + inputParts[1] + " room\n");
                            } else {
                                System.out.println("Invalid command room\n");
                            }
                        }

                    // else if the 'input' starts with the keyword '/users' get the list of all users by calling
                    // the remote method 'listUsers' method and display the returned 'returnValueParts'
                        
                    } else if (input.startsWith("/users")) {
                        String returnValue = serverInterfaceImpl.listUsers(connectedRoom);
                        String[] returnValueParts = returnValue.split(Pattern.quote(" "));

                        System.out.println("*** All Users ***\n");
                        for (int i = 0; i < returnValueParts.length; i++) {
                            System.out.println((i + 1) + ". " + returnValueParts[i] + "\n");
                        }
                        System.out.println("*******************\n");

                    // else if the 'input' starts with the keyword '/rooms' get the list of all rooms by calling
                    // the the remote method 'listRooms' method and display the returned 'returnValueParts'

                    } else if (input.startsWith("/rooms")) {
                        String returnValue = serverInterfaceImpl.listRooms();
                        String[] returnValueParts = returnValue.split(Pattern.quote(" "));

                        System.out.println("*** Rooms ***\n");
                        for (int i = 0; i < returnValueParts.length; i++) {
                            System.out.println((i + 1) + ". " + returnValueParts[i] + "\n");
                        }
                        System.out.println("*******************\n");

                    // else if the 'input' starts with the keyword '/create' create a new room by calling 
                    // the remote method 'createNewRooms' if it does not already exist
                        
                    } else if (input.startsWith("/create")) {
                        String[] inputParts = input.split(Pattern.quote(" "));
                        String response = serverInterfaceImpl.createNewRooms(inputParts[1]);
                        if (response.equals("exist")) {
                            System.out.println(inputParts[1] + " room does not exist\n");
                        } else if (response.equals("created")) {
                            System.out.println(inputParts[1] + " room was created\n");
                        }

                    // else send the message to all connected users by calling the remote method 'newMessages'
                    // the message contain the room to send 'connectedRoom', the 'userName' and the message 'input' 
                        
                    } else {
                        serverInterfaceImpl.newMessages(connectedRoom, "@" + userName + ":" + input);
                        
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("ERROR : " + e);
            e.printStackTrace(System.out);
        }
    }
}
