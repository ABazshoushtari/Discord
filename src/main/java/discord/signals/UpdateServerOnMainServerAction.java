package discord.signals;

import discord.mainServer.MainServer;
import discord.client.Server;

public class UpdateServerOnMainServerAction implements Action {
    private final Server server;

    public UpdateServerOnMainServerAction(Server server) {
        this.server = server;
    }

    @Override
    public Object act() {
        MainServer.getServers().replace(server.getUnicode(), server);
        return MainServer.updateDatabase(server);
    }
}
