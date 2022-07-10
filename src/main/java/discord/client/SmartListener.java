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

                    if (mainServerResponse instanceof ModelUpdaterSignal mus) {

                        if (!(mus instanceof FriendChangedModelUpdaterSignal)) {
                            mus.setBeingUpdatedModel(controller.getUser());
                            controller.setUser(mus.getUpdatedModel());
                        }

                        Platform.runLater(() -> {
                            try {
                                controller.setUpdatedValuesForObservableLists();
                                //controller.setUpdatedValuesForServerObservableLists();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                    }
                    else {
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
                        /*
                        switch (classSimpleName) {
                            case "FriendRequestSignal" -> {
                                FriendRequestSignal frs = (FriendRequestSignal) mainServerResponse;
                                controller.getUser().getIncomingFriendRequests().add(frs.requesterUID());
                                Platform.runLater(() -> {
                                    try {
                                        controller.refreshPending();
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                });
                            }
                            case "AcceptFriendRequestSignal" -> {
                                AcceptFriendRequestSignal afs = (AcceptFriendRequestSignal) mainServerResponse;
                                controller.getUser().getFriends().add(afs.accepterUID());
                                Platform.runLater(() -> {
                                    try {
                                        controller.refreshPending();
                                        controller.refreshFriends();
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                });
                            }
                            case "LostAFriendSignal" -> {
                                LostAFriendSignal lfs = (LostAFriendSignal) mainServerResponse;
                                controller.getUser().removeFriend(lfs.removerUID());
                                Platform.runLater(() -> {
                                    try {
                                        controller.refreshFriends();
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                });
                            }
                            case "FriendChangedSignal" -> Platform.runLater(() -> {
                                try {
                                    controller.refreshFriends();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        }
                    }*/
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
