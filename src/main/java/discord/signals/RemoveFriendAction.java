package discord.signals;

import discord.client.Model;
import discord.mainServer.MainServer;

import java.io.IOException;

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
        return MainServer.updateDatabase(remover) && MainServer.updateDatabase(beingRemoved);
    }
}
