package discord.signals;

import discord.client.Model;

public class LostAFriendModelUpdaterSignal extends ModelUpdaterSignal {

    private final Integer removerUID;

    public LostAFriendModelUpdaterSignal(Integer removerUID) {
        this.removerUID = removerUID;
    }

    @Override
    public Model getUpdatedModel() {
        beingUpdatedModel.removeFriend(removerUID);
        return beingUpdatedModel;
    }
}
