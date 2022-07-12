package discord.signals;

import discord.client.Model;

public class RespondFriendRequestModelUpdaterSignal extends ModelUpdaterSignal {

    private final Integer responderUID;
    private final Model responder;
    private final boolean accept;

    public RespondFriendRequestModelUpdaterSignal(Model responder, boolean accept) {
        this.responder = responder;
        responderUID = responder.getUID();
        this.accept = accept;
    }

    @Override
    public Model getUpdatedModel() {
        if (accept) {
            beingUpdatedModel.addFriend(responderUID);
        }
        beingUpdatedModel.getSentFriendRequests().remove(responderUID);
        return beingUpdatedModel;
    }

    public Integer getResponderUID() {
        return responderUID;
    }
    public Model getResponder() {
        return responder;
    }
    public boolean isAccept() {
        return accept;
    }
}
