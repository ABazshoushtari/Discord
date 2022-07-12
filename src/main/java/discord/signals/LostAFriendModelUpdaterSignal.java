package discord.signals;

import discord.client.Model;

public class LostAFriendModelUpdaterSignal extends ModelUpdaterSignal {

    //private final Model remover;
    //private final Integer removerUID;

    public LostAFriendModelUpdaterSignal(Model remover) {
        super(remover);
        //removerUID = remover.getUID();
    }

    @Override
    public Model getUpdatedModel() {
        beingUpdatedModel.removeFriend(((Model) beingChangedScreenElement).getUID());
        return beingUpdatedModel;
    }

//    public Model getRemover() {
//        return remover;
//    }
}
