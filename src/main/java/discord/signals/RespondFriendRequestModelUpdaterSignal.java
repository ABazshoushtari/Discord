package discord.signals;

import discord.client.Model;

public class RespondFriendRequestModelUpdaterSignal extends ModelUpdaterSignal {

    private final Integer acceptorUID;
    private final boolean accept;

    public RespondFriendRequestModelUpdaterSignal(Integer acceptorUID, boolean accept) {
        this.acceptorUID = acceptorUID;
        this.accept = accept;
    }

    @Override
    public Model getUpdatedModel() {
        if (accept) {
            beingUpdatedModel.addFriend(acceptorUID);
        }
        beingUpdatedModel.getSentFriendRequests().remove(acceptorUID);
        return beingUpdatedModel;
    }

    public Integer getAcceptorUID() {
        return acceptorUID;
    }
    public boolean isAccept() {
        return accept;
    }
}
