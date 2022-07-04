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
        if (!MainServer.getUsers().containsKey(username)) {
            return 0;
        } else {
            Model user = MainServer.getUsers().get(username);
            if (user.getFriendRequests().contains(requester)) {
                return 1;
            }
            if (user.getBlockedList().contains(requester)) {
                return 2;
            }
            user.getFriendRequests().add(requester);
            if (!MainServer.updateDatabase(user)) {
                return 3;
            }
            return 4;
        }
    }
}
