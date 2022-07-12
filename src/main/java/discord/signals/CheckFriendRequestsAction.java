package discord.signals;

import discord.mainServer.ClientHandler;
import discord.mainServer.MainServer;
import discord.client.Model;

import java.io.IOException;

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
            myUser.addFriend(requesterID);
            requester.addFriend(myUID);
        }

        requester.getSentFriendRequests().remove(myUID);
        myUser.getIncomingFriendRequests().remove(index);

        //MainServer.getUsers().replace(myUID, myUser);
        MainServer.updateDatabase(myUser);

        //MainServer.getUsers().replace(requesterID, requester);
        MainServer.updateDatabase(requester);

        for (ClientHandler ch : clientHandlers) {
            if (ch.getUser() != null) {
                if (ch.getUser().getUID().equals(requesterID)) {
                    synchronized (ch.getMySocket()) {
                        ch.getMySocket().write(new RespondFriendRequestModelUpdaterSignal(myUser, accept));
                    }
                    break;
                }
            }
        }

        return myUser;
    }
}
