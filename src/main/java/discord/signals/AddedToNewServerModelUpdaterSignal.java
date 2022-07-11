package discord.signals;

import discord.client.Model;

public class AddedToNewServerModelUpdaterSignal extends ModelUpdaterSignal {

    private final Integer newServerUnicode;

    public AddedToNewServerModelUpdaterSignal(Integer newServerUnicode) {
        this.newServerUnicode = newServerUnicode;
    }

    @Override
    public Model getUpdatedModel() {
        beingUpdatedModel.getServers().add(newServerUnicode);
        return beingUpdatedModel;
    }
}
