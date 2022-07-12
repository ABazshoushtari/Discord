package discord.signals;

import discord.client.Model;
import discord.client.Server;

public class AddedToNewServerModelUpdaterSignal extends ModelUpdaterSignal {

    private final Server newServer;
    private final Integer newServerUnicode;

    public AddedToNewServerModelUpdaterSignal(Server newServer) {
        this.newServer = newServer;
        newServerUnicode = newServer.getUnicode();
    }

    @Override
    public Model getUpdatedModel() {
        beingUpdatedModel.getServers().add(newServerUnicode);
        return beingUpdatedModel;
    }

    public Server getNewServer() {
        return newServer;
    }
}
