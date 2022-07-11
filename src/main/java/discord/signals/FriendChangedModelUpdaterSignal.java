package discord.signals;

import discord.client.Model;

public class FriendChangedModelUpdaterSignal extends ModelUpdaterSignal {

    @Override
    public Model getUpdatedModel() {
        return null;
    }
}
