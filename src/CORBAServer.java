
//AUTHOR: HABIB ADO SABIU

import ChatApp.*;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POA;

import java.util.*;
import java.util.regex.Pattern;

class ServerInterfaceImpl extends ServerInterfacePOA {

	// list to store all sent messages log
	static List<String> messageLogs = new ArrayList<>();

	// list to store all users together with their connected rooms
	static List<String> roomUsers = new ArrayList<>();

	// list to store all users name
	private static List<String> names = new ArrayList<>();
	// list to store all available rooms, the default room is 'general'
	private static List<String> rooms = new ArrayList<String>() {
		{
			add("general");
		}
	};

	// create an ORB object
	private ORB orb;

	// initialize the ORB object
	public void setORB(ORB orb_val) {
		orb = orb_val;
	}

	public String connection(String userName) {

		// create a 'StringBuilder sb' to store messages
		StringBuilder sb = new StringBuilder();

        // if names list already contains 'userName', return 'failure' message
        // else 
        //     add 'userName' to 'names' list
        //     append default room name to 'userName' and add it to 'roomUsers' list
        //     create a new message indicating a new user is connected and add it to 'messageLogs' list 
        //     get all the previously sent messages to this group and append it to a string builder (sb) object
        //     return a string builder (sb) object containing all previous messages in this group separated by | symbol
		if (names.contains(userName.toLowerCase())) {
			sb.append("failure");
		} else {
			String TimeStamp = new java.util.Date().toString();
			String connectedTime = "Connected on " + TimeStamp;
			names.add(userName);
			roomUsers.add("general " + userName);
			messageLogs.add("general @" + userName + " " + connectedTime);

			sb.append("joined");

			for (int i = 0; i < messageLogs.size(); i++) {
				if (messageLogs.get(i).startsWith("general")) {
					sb.append("|" + messageLogs.get(i));
				}
			}
		}

		return sb.toString();
	}

	// add a new message to the 'messageLogs' list, the new message contains 'roomName' followed by the 'message'
	public void newMessages(String roomName, String message) {
		messageLogs.add(roomName + " " + message);
	}

	// return the last message from 'roomName'
	public String getMessages(String roomName) {
		String valueToReturn = "";
		// get the last message from 'messageLogs'
		String message = messageLogs.get(messageLogs.size() - 1);

		// if the last message returned is for 'roomName', append it to 'valueToReturn'
		// otherwise, 'valueToReturn' will be null
		if (message.startsWith(roomName)) {
			valueToReturn = message.substring(message.indexOf(" ") + 1);
		}

		// return last message in 'roomName', or null if there is no new message for 'roomName'
		return valueToReturn;
	}

	// return the list of all connected users
	public String listUsers(String roomName) {

		StringBuilder sb = new StringBuilder();

		// loop through the names list and append all user names to a 'StringBuilder (sb)' object
		for (String s : names) {
			sb.append(s);
			sb.append(" ");
		}

		// return all users
		return sb.toString();
	}

	// return the list of all available rooms
	public String listRooms() {
		StringBuilder sb = new StringBuilder();

		// loop through the rooms list and append all available rooms to a 'StringBuilder (sb)' object
		for (String s : rooms) {
			sb.append(s);
			sb.append(" ");
		}

		// return all room names
		return sb.toString();
	}

	// create a new room 'roomName'
	public String createNewRooms(String roomName) {
		String response = "";

		// if 'roomName' already exist, return a failure message 'exist'
		// else, add 'roomName' to the rooms list and return a success message 'created'
		if (rooms.contains(roomName)) {
			response = "exist";
		} else {
			rooms.add(roomName);
			response = "created";
		}

		// return either success or failure
		return response;
	}

	// join an existing room
	public String joinRoom(String roomToJoin, String name) {
		StringBuilder response = new StringBuilder();

		// if 'roomToJoin' does not exist, return 'no-room' failure message
		// else, append 'roomToJoin' to 'name' and add it to the 'roomUsers' list,
		// 		return success message 'joined'
		if (!rooms.contains(roomToJoin)) {
			response.append("no-room");
		} else {
			roomUsers.add(roomToJoin + " " + name);
			messageLogs.add(roomToJoin + " " + name + " has joined");
			response.append("joined");

			for (int i = 0; i < messageLogs.size(); i++) {
				if (messageLogs.get(i).startsWith(roomToJoin)) {
					response.append("|" + messageLogs.get(i));
				}
			}
		}

		// return either success or failure message
		return response.toString();
	}

	// leave connected room
	public String leaveRoom(String roomToLeave, String name) {
		String response = "";

		// if 'rooms' does not contain 'roomToLeave' return 'no-room' failure
		// else if 'roomUsers' does not contain 'name' return 'no-user' failure
		// else remove 'name' from 'roomToLeave'
		// 		send message to 'roomToLeave' users indicating the user has left
		// 		return 'leave-success' success
		if (!rooms.contains(roomToLeave)) {
			response = "no-room";
		} else if (!roomUsers.contains(roomToLeave + " " + name)) {
			response = "no-user";
		} else {
			roomUsers.remove(roomToLeave + " " + name);
			messageLogs.add(roomToLeave + " " + name + " has left");
			response = "leave-success";
		}

		// return either success or failure message
		return response;
	}

	// disconnect from the chat application
	public void disconnect(String userName, String roomName) {
		// remove 'userName' from 'names' list
		names.remove(userName);
		// send message to 'roomName' users indicating the user has left
		messageLogs.add(roomName + " " + userName + " has left");
	}

}

public class CORBAServer {

	public static void main(String args[]) {

		try {
			// create and initialize the ORB
			ORB orb = ORB.init(args, null);

			// get reference to rootpoa & activate the POAManager
			POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
			rootpoa.the_POAManager().activate();

			// create servant and register it with the ORB
			ServerInterfaceImpl serverInterfaceImpl = new ServerInterfaceImpl();
			serverInterfaceImpl.setORB(orb);

			// get object reference from the servant
			org.omg.CORBA.Object ref = rootpoa.servant_to_reference(serverInterfaceImpl);
			ServerInterface href = ServerInterfaceHelper.narrow(ref);

			// get the root naming context
			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
			// Use NamingContextExt which is part of the Interoperable Naming Service (INS) specification.
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

			// bind the Object Reference in Naming
			String name = "ServerInterface";
			NameComponent path[] = ncRef.to_name(name);
			ncRef.rebind(path, href);

			System.out.println("Server running, accepting client connection...");

			// wait for invocations from clients
			orb.run();
		}

		catch (Exception e) {
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.out);
		}
	}
}