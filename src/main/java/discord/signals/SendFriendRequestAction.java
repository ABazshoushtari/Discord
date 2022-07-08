package discord.signals;

import discord.mainServer.MainServer;
import discord.client.Model;

public class SendFriendRequestAction implements Action {
    private final String requester;
    private final String username;

    public SendFriendRequestAction(String requester, String username) {
        this.requester = requester;
        this.username = username;
    }

    @Override
    public Object act() {
        Integer UID = MainServer.getIDs().getOrDefault(username, null);
        if (UID == null) {
            return 0;
        }
        if (!MainServer.getUsers().containsKey(UID)) {
            return 0;
        } else {
            Model user = MainServer.getUsers().get(UID);
            int requesterID = MainServer.getIDs().get(requester);
            if (user.getIncomingFriendRequests().contains(requesterID)) {
                return 1;
            }
            if (user.getBlockedList().contains(requesterID)) {
                return 2;
            }
            user.getIncomingFriendRequests().add(requesterID);
            if (!MainServer.updateDatabase(user)) {
                return 3;
            }
            return 4;
        }
    }
}
