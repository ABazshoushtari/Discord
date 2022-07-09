package discord.signals;

import discord.client.Model;

public class AcceptFriendRequestModelUpdaterSignal extends ModelUpdaterSignal {

    private final Integer acceptorUID;

    public AcceptFriendRequestModelUpdaterSignal(Integer acceptorUID) {
        this.acceptorUID = acceptorUID;
    }

    @Override
    public Model getUpdatedModel() {
        beingUpdatedModel.getFriends().add(acceptorUID);
        return beingUpdatedModel;
    }
}
