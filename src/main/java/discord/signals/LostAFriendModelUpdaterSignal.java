package discord.signals;

import discord.client.Model;

public class LostAFriendModelUpdaterSignal extends ModelUpdaterSignal {

    private final Model remover;
    private final Integer removerUID;

    public LostAFriendModelUpdaterSignal(Model remover) {
        this.remover = remover;
        removerUID = remover.getUID();
    }

    @Override
    public Model getUpdatedModel() {
        beingUpdatedModel.removeFriend(removerUID);
        return beingUpdatedModel;
    }

    public Model getRemover() {
        return remover;
    }
}
