package discord.signals;

import discord.client.Model;
import discord.client.Server;

public class AddedToNewServerModelUpdaterSignal extends ModelUpdaterSignal {

    public AddedToNewServerModelUpdaterSignal(Server newServer) {
        super(newServer);
    }

    @Override
    public Model getUpdatedModel() {
        beingUpdatedModel.getServers().add(((Server) beingChangedScreenElement).getUnicode());
        return beingUpdatedModel;
    }
}
