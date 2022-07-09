package discord.signals;

import discord.mainServer.MainServer;
import discord.client.Server;

public class AddNewServerToDatabaseAction implements Action {
    private final Server newServer;

    public AddNewServerToDatabaseAction(Server newServer) {
        this.newServer = newServer;
    }

    @Override
    public Object act() {
        MainServer.getServers().put(newServer.getUnicode(), newServer);
        MainServer.updateDatabase(newServer);
        return null;
    }
}
