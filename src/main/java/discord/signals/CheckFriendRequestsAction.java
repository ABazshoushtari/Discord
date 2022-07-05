package discord.signals;

import discord.mainServer.MainServer;
import discord.client.Model;

import java.util.ArrayList;

public class CheckFriendRequestsAction implements Action {
    private final String myUsername;
    private final int index;
    private final boolean accept;

    public CheckFriendRequestsAction(String myUsername, int index, boolean accept) {
        this.myUsername = myUsername;
        this.index = index;
        this.accept = accept;
    }

    @Override
    public Object act() {
        int myUID = MainServer.getIDs().get(myUsername);
        if (!MainServer.getUsers().containsKey(myUID)) {
            return null;
        }
        Model myUser = MainServer.getUsers().get(myUID);

        int requesterID = myUser.getFriendRequests().get(index);
        Model requester = MainServer.getUsers().get(requesterID);

        boolean DBConnect = true;
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

            DBConnect = MainServer.updateDatabase(requester);
        }

        myUser.getFriendRequests().remove(index);

        DBConnect = DBConnect && MainServer.updateDatabase(myUser);

        return DBConnect;
    }
}
