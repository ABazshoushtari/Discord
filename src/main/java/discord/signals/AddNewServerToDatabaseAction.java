package discord.signals;

import discord.client.Model;
import discord.mainServer.MainServer;
import discord.client.Server;

public class AddNewServerToDatabaseAction implements Action {
    private final Server newServer;

    public AddNewServerToDatabaseAction(Server newServer) {
        this.newServer = newServer;
    }

    @Override
    public Object act() {

        Model creator = MainServer.getUsers().get(newServer.getCreatorUID());
        creator.getServers().add(newServer.getUnicode());
        MainServer.getUsers().replace(creator.getUID(), creator);

        MainServer.getServers().put(newServer.getUnicode(), newServer);
        MainServer.updateDatabaseAndMainServer(newServer);

        return null;
    }
}
