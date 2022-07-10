package discord.signals;

import discord.client.Model;

public class CancelFriendRequestModelUpdaterSignal extends ModelUpdaterSignal {

    private final Integer cancellerUID;

    public CancelFriendRequestModelUpdaterSignal(Integer cancellerUID) {
        this.cancellerUID = cancellerUID;
    }

    @Override
    public Model getUpdatedModel() {
        beingUpdatedModel.getIncomingFriendRequests().remove(cancellerUID);
        return beingUpdatedModel;
    }
}
