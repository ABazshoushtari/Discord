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
                        // a signal generated from another client to inform us of a change
                        // these signals are sent by other users in the act method of Actions to inform us something
                        if (!(mus instanceof RelatedUserChangedSignal)) {
                            mus.setBeingUpdatedModel(controller.getUser());
                            controller.setUser(mus.getUpdatedModel());  // update receiver of the signal
                        }

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                // maybe synchronized is necessary because another thread(smartListener) is working with controller
                                // maybe it should be locked on ObservableLists except refreshServers and refreshPrivateChats
                                synchronized (controller) {
                                    if (mus instanceof RelatedUserChangedSignal relatedUserChangedSignal) {
                                        try {
                                            controller.refreshEverything();
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
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

                                    } else if (mus instanceof RespondFriendRequestModelUpdaterSignal respondFriendRequestModelUpdaterSignal) {
                                        Model responder = respondFriendRequestModelUpdaterSignal.getResponder();
                                        Platform.runLater(new Runnable() {
                                            @Override
                                            public void run() {
                                                controller.getPendingObservableList().remove(responder);
                                                if (respondFriendRequestModelUpdaterSignal.isAccept()) {
                                                    controller.getAllFriendsObservableList().add(responder);
                                                    controller.getDirectMessagesObservableList().add(responder);
                                                    if (responder.getStatus() != Status.Invisible) {
                                                        controller.getOnlineFriendsObservableList().add(responder);
                                                    }
                                                }
                                            }
                                        });
                                    } else if (mus instanceof FriendRequestModelUpdaterSignal friendRequestModelUpdaterSignal) {
                                        Platform.runLater(new Runnable() {
                                            @Override
                                            public void run() {
                                                controller.getPendingObservableList().add(friendRequestModelUpdaterSignal.getRequester());
                                            }
                                        });
                                    } else if (mus instanceof CancelFriendRequestModelUpdaterSignal cancelFriendRequestModelUpdaterSignal) {
                                        Platform.runLater(new Runnable() {
                                            @Override
                                            public void run() {
                                                controller.getPendingObservableList().remove(cancelFriendRequestModelUpdaterSignal.getCanceller());
                                            }
                                        });
                                    } else if (mus instanceof LostAFriendModelUpdaterSignal lostAFriendModelUpdaterSignal) {
                                        Platform.runLater(new Runnable() {
                                            @Override
                                            public void run() {
                                                Model remover = lostAFriendModelUpdaterSignal.getRemover();
                                                controller.getAllFriendsObservableList().remove(remover);
                                                controller.getOnlineFriendsObservableList().remove(remover);
                                                controller.getDirectMessagesObservableList().remove(remover);
                                            }
                                        });
                                    } else if (mus instanceof ChatMessageSignal chatMessageSignal) {
                                        Platform.runLater(new Runnable() {
                                            @Override
                                            public void run() {
                                                controller.getChatMessageObservableList().add(chatMessageSignal.getChatMessage());
                                            }
                                        });
                                    } else if (mus instanceof AddedToNewServerModelUpdaterSignal addedToNewServerModelUpdaterSignal) {
                                        Platform.runLater(new Runnable() {
                                            @Override
                                            public void run() {
                                                controller.getServersObservableList().add(addedToNewServerModelUpdaterSignal.getNewServer());
                                            }
                                        });
                                    }
                                }
                            }
                        });


                        /*
                        Platform.runLater(() -> {
                            try {
                                switch (mus.getClass().getSimpleName()) {

                                    case "RelatedUserChangedSignal" -> controller.refreshEverything();  // odd(distinctive) signal

                                    case "RespondFriendRequestModelUpdaterSignal" -> {
                                        controller.refreshPending();
                                        controller.refreshFriends();
                                    }
                                    case "CancelFriendRequestModelUpdaterSignal",
                                            "FriendRequestModelUpdaterSignal" -> controller.refreshPending();
                                    case "LostAFriendModelUpdaterSignal" -> controller.refreshFriends();
                                    case "ChatMessageSignal" ->
                                            controller.getChatMessageObservableList().add(((ChatMessageSignal) mus).getChatMessage());
                                    case "AddedToNewServerModelUpdaterSignal" -> controller.refreshServers();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                         */

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
