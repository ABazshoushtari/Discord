package discord.signals;

import discord.client.Model;
import discord.client.Server;

public class GotRemovedFromAServerModelUpdaterSignal extends ModelUpdaterSignal {

    public GotRemovedFromAServerModelUpdaterSignal(Object server) {
        super(server);
    }

    @Override
    public Model getUpdatedModel() {
        beingUpdatedModel.getServers().remove(((Server) beingChangedScreenElement).getUnicode());
        return beingUpdatedModel;
    }
}
