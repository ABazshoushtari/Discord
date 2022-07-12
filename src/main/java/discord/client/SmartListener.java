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

                    if (mainServerResponse instanceof UpdaterSignal updaterSignal) {

                        // a signal generated from another client to inform us of a change
                        // these signals are sent by other users in the act method of Actions to inform us something

                        if (updaterSignal instanceof ModelUpdaterSignal mus) {
                            mus.setBeingUpdatedModel(controller.getUser());
                            controller.setUser(mus.getUpdatedModel());  // update receiver of the signal
                        }

                        // maybe synchronized is necessary because another thread(smartListener) is working with controller
                        // maybe it should be locked on ObservableLists except refreshServers and refreshPrivateChats

                        Platform.runLater(() -> {
                            synchronized (controller) {
                                if (updaterSignal instanceof ModelUpdaterSignal mus) {
                                    Object beingChangedScreenElement = mus.getBeingChangedScreenElement();
                                    switch (mus.getClass().getSimpleName()) {
                                        case "RespondFriendRequestModelUpdaterSignal" -> {
                                            Model responder = (Model) beingChangedScreenElement;

                                            controller.addOrRemoveFromPendingList(responder, false);

                                            boolean accept = ((RespondFriendRequestModelUpdaterSignal) mus).isAccept();
                                            if (accept) {
                                                controller.addOrRemoveFromEveryFriendList(responder, true);
                                            }
                                        }

                                        case "FriendRequestModelUpdaterSignal" -> {
                                            Model requester = (Model) beingChangedScreenElement;
                                            controller.addOrRemoveFromPendingList(requester, true);
                                        }

                                        case "CancelFriendRequestModelUpdaterSignal" -> {
                                            Model canceller = (Model) beingChangedScreenElement;
                                            controller.addOrRemoveFromPendingList(canceller, false);
                                        }

                                        case "LostAFriendModelUpdaterSignal" -> {
                                            Model remover = (Model) beingChangedScreenElement;
                                            controller.addOrRemoveFromEveryFriendList(remover, false);
                                        }

                                        case "ChatMessageSignal" -> {
                                            ChatMessage chatMessage = (ChatMessage) beingChangedScreenElement;
                                            controller.getChatMessageObservableList().add(chatMessage);
                                        }

                                        case "AddedToNewServerModelUpdaterSignal" -> {
                                            Server newServer = (Server) beingChangedScreenElement;
                                            controller.getServersObservableList().add(newServer);
                                        }

                                        case "GotRemovedFromAServerModelUpdaterSignal" -> {
                                            Server lostServer = (Server) beingChangedScreenElement;
                                            controller.getServersObservableList().remove(lostServer);
                                        }
                                    }
                                } else {
                                    switch (updaterSignal.getClass().getSimpleName()) {
                                        case "RelatedUserChangedUpdaterSignal" -> {
                                            try {
                                                controller.refreshEverything();
                                                controller.refreshEveryServerThing();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        case "RelatedServerChangedUpdaterSignal" -> {
                                            try {
                                                controller.refreshEveryServerThing();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }

                                        }
                                    }
                                }
                            }
                        });

                    } else {    // the write-back of the written action by the user themselves (get part of "send and get response")
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

//                                        Platform.runLater(new Runnable() {
//                                            @Override
//                                            public void run() {
//                                                Model relatedUserChanged = relatedUserChangedSignal.getUpdatedModel();
//                                                int index = -1;
//
//                                                index = controller.getBlockedPeopleObservableList().indexOf(relatedUserChanged);
//                                                if (index >= 0) {
//                                                    controller.getBlockedPeopleObservableList().set(index, relatedUserChanged);
//                                                }
//
//                                                index = controller.getPendingObservableList().indexOf(relatedUserChanged);
//                                                if (index >= 0) {
//                                                    controller.getPendingObservableList().set(index, relatedUserChanged);
//                                                }
//
//                                                index = controller.getAllFriendsObservableList().indexOf(relatedUserChanged);
//                                                if (index >= 0) {
//                                                    controller.getAllFriendsObservableList().set(index, relatedUserChanged);
//                                                }
//
//                                                index = controller.getOnlineFriendsObservableList().indexOf(relatedUserChanged);
//                                                if (index >= 0) {
//                                                    controller.getOnlineFriendsObservableList().set(index, relatedUserChanged);
//                                                }
//
//                                                index = controller.getDirectMessagesObservableList().indexOf(relatedUserChanged);
//                                                if (index >= 0) {
//                                                    controller.getDirectMessagesObservableList().set(index, relatedUserChanged);
//                                                }
//
//                                                try {
//                                                    controller.refreshServers();
//                                                } catch (IOException e) {
//                                                    throw new RuntimeException(e);
//                                                }
//                                                controller.refreshPrivateChat();
//                                            }
//                                        });
