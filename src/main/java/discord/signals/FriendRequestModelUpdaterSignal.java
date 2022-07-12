package discord.signals;

import discord.client.Model;

public class FriendRequestModelUpdaterSignal extends ModelUpdaterSignal {

    private final Model requester;
    private final Integer requesterUID;

    public FriendRequestModelUpdaterSignal(Model requester) {
        this.requester = requester;
        requesterUID = requester.getUID();
    }

    @Override
    public Model getUpdatedModel() {
        beingUpdatedModel.getIncomingFriendRequests().add(requesterUID);
        return beingUpdatedModel;
    }

    public Model getRequester() {
        return requester;
    }
}
