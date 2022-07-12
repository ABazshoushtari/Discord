package discord.signals;

import discord.client.Model;
import discord.mainServer.ClientHandler;
import discord.mainServer.MainServer;

import java.io.IOException;

import static discord.mainServer.ClientHandler.clientHandlers;

public class RemoveFriendAction implements Action {

    private final Integer removerUID;
    private final Integer beingRemovedUID;

    public RemoveFriendAction(Integer removerUID, Integer beingRemovedUID) {
        this.removerUID = removerUID;
        this.beingRemovedUID = beingRemovedUID;
    }

    @Override
    public Object act() throws IOException {

        Model remover = MainServer.getUsers().get(removerUID);
        Model beingRemoved = MainServer.getUsers().get(beingRemovedUID);

        remover.getFriends().remove(beingRemovedUID);
        beingRemoved.getFriends().remove(removerUID);
        for (ClientHandler ch : clientHandlers) {
            if (ch.getUser() != null) {
                if (ch.getUser().getUID().equals(beingRemovedUID)) {
                    synchronized (ch.getMySocket()) {
                        ch.getMySocket().write(new LostAFriendModelUpdaterSignal(remover));
                    }
                    break;
                }
            }
        }

        //MainServer.getUsers().replace(removerUID, remover);
        MainServer.updateDatabase(remover);

        //MainServer.getUsers().replace(beingRemovedUID, beingRemoved);
        MainServer.updateDatabase(beingRemoved);
        return null;
    }
}
