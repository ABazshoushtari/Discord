package discord.signals;

import discord.mainServer.ClientHandler;
import discord.mainServer.MainServer;
import discord.client.Model;

import java.io.IOException;

import static discord.mainServer.ClientHandler.clientHandlers;

public class AddFriendToServerAction implements Action {
    private final Integer unicode;
    private final Integer newMemberUID;

    public AddFriendToServerAction(Integer unicode, Integer newMemberUID) {
        this.unicode = unicode;
        this.newMemberUID = newMemberUID;
    }

    @Override
    public Object act() throws IOException {

        Model targetFriend = MainServer.getUsers().get(newMemberUID);
        targetFriend.getServers().add(unicode);

        MainServer.updateDatabase(targetFriend);

        for (ClientHandler ch : clientHandlers) {
            Model user = ch.getUser();
            if (user != null) {
                if (user.getUID().equals(newMemberUID)) {
                    synchronized (ch.getMySocket()) {
                        ch.getMySocket().write(new AddedToNewServerModelUpdaterSignal(MainServer.getServers().get(unicode)));
                    }
                    break;
                }
            }
        }

        return null;
    }
}
