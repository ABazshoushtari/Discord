package discord.client;

import discord.signals.*;
import javafx.application.Platform;

import java.io.IOException;

public class SmartListener implements Runnable {
    private final Controller controller;
    private Boolean receivedBoolean;
    private Model receivedUser;
    private Server receivedServer;
    private Integer receivedInteger;

    public SmartListener(Controller controller) {
        this.controller = controller;
    }

    @Override
    public void run() {

        MySocket mySocket = controller.getMySocket();
        while (mySocket.isConnected()) {

            try {
                Object mainServerResponse = mySocket.read();
                if (mainServerResponse != null) {

                    if (mainServerResponse instanceof UpdaterSignal us) {

                        // a signal generated from another client to inform us of a change

                        if (us instanceof ModelUpdaterSignal mus) {
                            mus.setBeingUpdatedModel(controller.getUser());
                            controller.setUser(mus.getUpdatedModel());
                        }


                        Platform.runLater(() -> {
                            try {
                                switch (us.getClass().getSimpleName()) {
                                    case "RelatedUserChangedUpdaterSignal" -> {
                                        controller.refreshEverything();
                                        // controller -> refresh us.getID() related people
                                    }
                                    case "AServerIsChangedSignal" -> {
                                        if (controller.getCurrentServer().getUnicode().equals(us.getID())) {
                                            controller.setUpdatedValuesForServerObservableLists();
                                        }
                                    }
                                    case "RespondFriendRequestModelUpdaterSignal" -> {
                                        controller.refreshPending();
                                        controller.refreshFriends();
                                    }
                                    case "CancelFriendRequestModelUpdaterSignal",
                                            "FriendRequestModelUpdaterSignal" -> controller.refreshPending();
                                    case "LostAFriendModelUpdaterSignal" -> controller.refreshFriends();
                                    case "ChatMessageSignal" -> controller.refreshPrivateChat();
                                    case "AddedToNewServerModelUpdaterSignal" -> controller.refreshServers();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });

                    } else {    // the write-back of the written action by the user themselves
                        synchronized (controller) {
                            switch (mainServerResponse.getClass().getSimpleName()) {
                                case "Model" -> receivedUser = (Model) mainServerResponse;
                                case "Boolean" -> receivedBoolean = (Boolean) mainServerResponse;
                                case "Server" -> receivedServer = (Server) mainServerResponse;
                                case "Integer" -> receivedInteger = (Integer) mainServerResponse;
                            }
                            controller.notify();
                        }
                    }
                } else {
                    synchronized (controller) {
                        receiveNull();
                        controller.notify();
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }

        }
    }

    private void receiveNull() {
        receivedBoolean = null;
        receivedUser = null;
        receivedServer = null;
        receivedInteger = null;
    }

    public Boolean getReceivedBoolean() {
        return receivedBoolean;
    }

    public Model getReceivedUser() {
        return receivedUser;
    }

    public Server getReceivedServer() {
        return receivedServer;
    }

    public Integer getReceivedInteger() {
        return receivedInteger;
    }
}
