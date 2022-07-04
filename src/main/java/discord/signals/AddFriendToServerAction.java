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
        Model targetFriend = MainServer.getUsers().get(friendUsername);
        targetFriend.getServers().add(unicode);
        return MainServer.updateDatabase(targetFriend);
    }
}
