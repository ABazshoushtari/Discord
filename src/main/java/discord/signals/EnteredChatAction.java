package discord.signals;

import discord.client.Model;
import discord.mainServer.MainServer;

import java.io.IOException;

public class EnteredChatAction implements Action {

    private final Integer myUID;
    private final Integer friendUID;

    public EnteredChatAction(Integer myUID, Integer friendUID) {
        this.myUID = myUID;
        this.friendUID = friendUID;
    }

    @Override
    public Object act() throws IOException {

        Model me = MainServer.getUsers().get(myUID);

        me.getIsInChat().replace(friendUID, true);

        MainServer.getUsers().replace(myUID, me);
        MainServer.updateDatabase(me);

        return null;
    }
}
