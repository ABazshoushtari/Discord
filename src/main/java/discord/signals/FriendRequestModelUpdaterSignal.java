package discord.signals;

import discord.client.Model;

public class FriendRequestModelUpdaterSignal extends ModelUpdaterSignal {

    private final Integer requesterUID;

    public FriendRequestModelUpdaterSignal(Integer requesterUID) {
        this.requesterUID = requesterUID;
    }

    @Override
    public Model getUpdatedModel() {
        beingUpdatedModel.getIncomingFriendRequests().add(requesterUID);
        return beingUpdatedModel;
    }
}
