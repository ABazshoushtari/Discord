package discord.signals;

import discord.client.Model;

public class RespondFriendRequestModelUpdaterSignal extends ModelUpdaterSignal {

    //private final Model responder;
    private final boolean accept;

    public RespondFriendRequestModelUpdaterSignal(Model responder, boolean accept) {
        super(responder);
//        this.responder = responder;
        this.accept = accept;
    }

    @Override
    public Model getUpdatedModel() {
        if (accept) {
            beingUpdatedModel.addFriend(((Model) getBeingChangedScreenElement()).getUID());
        }
        beingUpdatedModel.getSentFriendRequests().remove(((Model) beingChangedScreenElement).getUID());
        return beingUpdatedModel;
    }

//    public Model getResponder() {
//        return responder;
//    }

    public boolean isAccept() {
        return accept;
    }
}
