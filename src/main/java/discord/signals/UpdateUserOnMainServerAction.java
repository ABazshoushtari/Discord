package discord.signals;

import discord.mainServer.MainServer;
import discord.client.Model;

public class UpdateUserOnMainServerAction implements Action {
    private final Model me;
    private final String oldUsername;

    public UpdateUserOnMainServerAction(Model me, String oldUsername) {
        this.me = me;
        this.oldUsername = oldUsername;
    }

    @Override
    public Object act() {
        MainServer.getIDs().remove(oldUsername);
        MainServer.getIDs().put(me.getUsername(), me.getUID());
        MainServer.getUsers().replace(me.getUID(), me);
        return MainServer.updateDatabase(me);
    }
}