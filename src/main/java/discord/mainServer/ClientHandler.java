package discord.mainServer;

import discord.client.*;
import discord.signals.*;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class ClientHandler implements Runnable {
    // Fields:
    public static List<ClientHandler> clientHandlers = Collections.synchronizedList(new ArrayList<>());
    private Model user;
    private final MySocket mySocket;

    // Constructors:
    public ClientHandler(Socket socket) {
        mySocket = new MySocket(socket);
        clientHandlers.add(this);
    }

    // Getters:
    public Model getUser() {
        return user;
    }

    public MySocket getMySocket() {
        return mySocket;
    }

    // Methods:
    @Override
    public void run() {
        while (true) {
            try {
                Action action;
                // the first while loop is for signing up or logging in
                while (user == null) {
                    action = mySocket.read();
                    if (action instanceof LoginAction || ((action instanceof SignUpOrChangeInfoAction && ((SignUpOrChangeInfoAction) action).getStage() == 5))) {
                        user = (Model) action.act();
                        // write back the Model of the logged in/signed-up user
                        mySocket.write(user);
                    } else {
                        // used for signing up stages before finalizing the registration (validating fields)
                        mySocket.write(action.act());
                    }
                }
                // the second while loop is for any other action after logging in or signing up
                while (user != null) {
                    action = mySocket.read();
                    if (action instanceof UpdateUserOnMainServerAction) {
                        user = (Model) action.act();
                    } else if (action instanceof LogoutAction) {
                        user.setStatus(Status.Invisible);
                        MainServer.getUsers().replace(user.getUID(), user);
                        boolean DBConnect = MainServer.updateDatabase(user);
                        user = null;
                    } else {
                        mySocket.write(action.act());
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                clientHandlers.remove(this);
                if (user != null) {
                    try {
                        handleForceQuit();
                    } catch (IOException ex) {
                       ex.printStackTrace();
                    }
                }
                mySocket.closeEverything();
                if (user != null) {
                    System.out.println("clientHandler of " + user.getUsername() + " got removed");
                } else {
                    System.out.println("clientHandler of a not logged in client got removed");
                }
                break;
            }
        }
    }

    private void handleForceQuit() throws IOException {
        user.setStatus(Status.Invisible);
        for (ClientHandler ch : clientHandlers) {
            if (user.getFriends().contains(ch.getUser().getUID())) {
                ch.mySocket.write(new FriendChangedStatusSignal());
            }
        }
        MainServer.getUsers().replace(user.getUID(), user);
        MainServer.updateDatabase(user);
    }
}
