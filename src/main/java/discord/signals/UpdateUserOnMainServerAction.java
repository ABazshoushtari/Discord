package discord.signals;

import discord.mainServer.MainServer;
import discord.client.Model;

public class UpdateUserOnMainServerAction implements Action {
    private final Model me;

    public UpdateUserOnMainServerAction(Model me) {
        this.me = me;
    }

    @Override
    public Object act() {
        MainServer.getUsers().replace(me.getUID(), me);
        return MainServer.updateDatabase(me);
    }
}