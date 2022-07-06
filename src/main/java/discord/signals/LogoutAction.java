package discord.signals;

import discord.client.Model;
import discord.client.Status;
import discord.mainServer.MainServer;

import java.io.IOException;

public class LogoutAction implements Action {

    private final Model me;

    public LogoutAction(Model me) {
        this.me = me;
    }

    @Override
    public Object act() throws IOException {
        me.setPreviousSetStatus(me.getStatus());
        me.setStatus(Status.Invisible);
        MainServer.getUsers().replace(me.getUID(), me);
        return MainServer.updateDatabase(me);
    }
}
