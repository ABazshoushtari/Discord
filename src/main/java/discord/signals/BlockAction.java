package discord.signals;

import discord.client.Model;
import discord.mainServer.MainServer;

public class BlockAction implements Action {
    private final String blocker;
    private final String beingBlocked;

    public BlockAction(String blocker, String beingBlocked) {
        this.blocker = blocker;
        this.beingBlocked = beingBlocked;
    }

    @Override
    public Object act() {
        if (!MainServer.getUsers().containsKey(beingBlocked)) {
            return null;
        } else {

            Model blockerUser = MainServer.getUsers().get(blocker);
            blockerUser.getFriendRequests().remove(beingBlocked);
            blockerUser.getFriends().remove(beingBlocked);
            blockerUser.getBlockedList().add(beingBlocked);

            Model beingBlockerUser = MainServer.getUsers().get(beingBlocked);
            beingBlockerUser.getFriendRequests().remove(blocker);
            beingBlockerUser.getFriends().remove(blocker);

            return MainServer.updateDatabase(blockerUser) && MainServer.updateDatabase(beingBlockerUser);
        }
    }
}