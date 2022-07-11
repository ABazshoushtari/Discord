package discord.signals;

import discord.mainServer.MainServer;
import discord.client.Model;

public class AddFriendToServerAction implements Action {
    private final int unicode;
    private final String friendUsername;

    public AddFriendToServerAction(int unicode, String friendUsername) {
        this.unicode = unicode;
        this.friendUsername = friendUsername;
    }

    @Override
    public Object act() {
        int friendID = MainServer.getIDs().get(friendUsername);
        Model targetFriend = MainServer.getUsers().get(friendID);
        targetFriend.getServers().add(unicode);

        MainServer.getUsers().replace(friendID, targetFriend);
        MainServer.updateDatabase(targetFriend);
        return null;
    }
}
