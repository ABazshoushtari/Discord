package discord.signals;

import discord.client.Model;

public class RelatedUserChangedSignal extends ModelUpdaterSignal {

    @Override
    public Model getUpdatedModel() {
        return null;
    }
}
