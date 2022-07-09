package discord.signals;

import discord.mainServer.ClientHandler;
import discord.mainServer.MainServer;
import discord.client.Model;

import java.io.IOException;
import java.util.ArrayList;

import static discord.mainServer.ClientHandler.clientHandlers;

public class CheckFriendRequestsAction implements Action {
    private final Integer myUID;
    private final int index;
    private final boolean accept;

    public CheckFriendRequestsAction(Integer myUID, int index, boolean accept) {
        this.myUID = myUID;
        this.index = index;
        this.accept = accept;
    }

    @Override
    public Object act() throws IOException {

        Model myUser = MainServer.getUsers().get(myUID);

        int requesterID = myUser.getIncomingFriendRequests().get(index);
        Model requester = MainServer.getUsers().get(requesterID);

        if (accept) {

            myUser.getFriends().add(requesterID);
            requester.getFriends().add(myUID);

            myUser.getIsInChat().put(requesterID, false);
            myUser.getPrivateChats().put(requesterID, new ArrayList<>());
            myUser.getUrlsOfPrivateChat().put(requesterID, new ArrayList<>());
            myUser.getFilesOfPrivateChat().put(requesterID, new ArrayList<>());

            requester.getIsInChat().put(myUID, false);
            requester.getPrivateChats().put(myUID, new ArrayList<>());
            requester.getUrlsOfPrivateChat().put(myUID, new ArrayList<>());
            requester.getFilesOfPrivateChat().put(myUID, new ArrayList<>());

            for (ClientHandler ch : clientHandlers) {
                if (ch.getUser() != null) {
                    if (ch.getUser().getUID().equals(requesterID)) {
                        ch.getMySocket().write(new AcceptFriendRequestModelUpdaterSignal(myUID));
                        break;
                    }
                }
            }
            MainServer.updateDatabase(requester);
        }

        myUser.getIncomingFriendRequests().remove(index);
        MainServer.updateDatabase(myUser);

        return myUser;
    }
}
