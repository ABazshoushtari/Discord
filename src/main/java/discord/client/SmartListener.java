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
                        //              if (!(mus instanceof RelatedUserChangedSignal)) {

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
                                            controller.getPendingObservableList().remove(responder);
                                            if (((RespondFriendRequestModelUpdaterSignal) mus).isAccept()) {
                                                controller.getAllFriendsObservableList().add(responder);
                                                controller.getDirectMessagesObservableList().add(responder);
                                                if (responder.getStatus() != Status.Invisible) {
                                                    controller.getOnlineFriendsObservableList().add(responder);
                                                }
                                            }
                                        }

                                        case "FriendRequestModelUpdaterSignal" -> {
                                            Model requester = (Model) beingChangedScreenElement;
                                            controller.getPendingObservableList().add(requester);
                                        }

                                        case "CancelFriendRequestModelUpdaterSignal" -> {
                                            Model canceller = (Model) beingChangedScreenElement;
                                            controller.getPendingObservableList().remove(canceller);
                                        }

                                        case "LostAFriendModelUpdaterSignal" -> {
                                            Model remover = (Model) beingChangedScreenElement;

                                            controller.getAllFriendsObservableList().remove(remover);
                                            controller.getOnlineFriendsObservableList().remove(remover);
                                            controller.getDirectMessagesObservableList().remove(remover);
                                        }

                                        case "ChatMessageSignal" -> {
                                            ChatMessage chatMessage = (ChatMessage) beingChangedScreenElement;
                                            controller.getChatMessageObservableList().add(chatMessage);
                                        }

                                        case "AddedToNewServerModelUpdaterSignal" -> {
                                            Server newServer = (Server) beingChangedScreenElement;
                                            controller.getServersObservableList().add(newServer);
                                        }
                                    }
                                } else {
                                    switch (updaterSignal.getClass().getSimpleName()) {
                                        case "RelatedUserChangedUpdaterSignal" -> {
                                            try {
                                                controller.refreshEverything();
                                            } catch (IOException e) {
                                                throw new RuntimeException(e);
                                            }
                                        }
                                        case "RelatedServerChangedUpdaterSignal" -> {
                                            if (controller.getCurrentServer().getUnicode().equals(updaterSignal.getID())) {
                                                try {
                                                    controller.setUpdatedValuesForServerObservableLists();
                                                } catch (IOException e) {
                                                    throw new RuntimeException(e);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        });
                        /*
                        Platform.runLater(() -> {
                            try {
                                switch (mus.getClass().getSimpleName()) {

                                    case "RelatedUserChangedSignal" -> controller.refreshEverything();  // odd(distinctive) signal


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
