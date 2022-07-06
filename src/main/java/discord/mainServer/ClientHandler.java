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
                        if (user != null) {
                            if (user.getStatus() == null) {
                                user.setStatus(Status.Online);
                            }
                        }
                        // write back the Model of the logged in/ signed-up user
                        mySocket.write(user);
                    } else {
                        // used for signing up stages before finalizing the registration (validating fields)
                        mySocket.write(action.act());
                    }
                }
                // the second while loop is for any other action after logging in or signing up
                while (user != null) {
                    action = mySocket.read();
                    mySocket.write(action.act());
                    if (action instanceof LogoutAction) {
                        user = null;
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                clientHandlers.remove(this);
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
}
