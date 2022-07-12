package discord.signals;

import discord.client.Model;

public class CancelFriendRequestModelUpdaterSignal extends ModelUpdaterSignal {

//    private final Model canceller;
//    private final Integer cancellerUID;

    public CancelFriendRequestModelUpdaterSignal(Model canceller) {
        super(canceller);
        //this.canceller = canceller;
        //cancellerUID = canceller.getUID();
    }

    @Override
    public Model getUpdatedModel() {
        beingUpdatedModel.getIncomingFriendRequests().remove(((Model) beingChangedScreenElement).getUID());
        return beingUpdatedModel;
    }

//    public Model getCanceller() {
//        return canceller;
//    }
}
