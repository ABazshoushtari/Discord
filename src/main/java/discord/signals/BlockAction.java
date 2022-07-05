package discord.signals;

import discord.client.Model;
import discord.mainServer.MainServer;

public class BlockAction implements Action {
    private final String blockerUsername;
    private final String beingBlockedUsername;

    public BlockAction(String blockerUsername, String beingBlockedUsername) {
        this.blockerUsername = blockerUsername;
        this.beingBlockedUsername = beingBlockedUsername;
    }

    @Override
    public Object act() {

        int beingBlockedID = MainServer.getIDs().get(beingBlockedUsername);

        if (!MainServer.getUsers().containsKey(beingBlockedID)) {
            return null;
        } else {

            int blockerID = MainServer.getIDs().get(blockerUsername);
            Model blockerUser = MainServer.getUsers().get(blockerID);
            blockerUser.getFriendRequests().remove(beingBlockedID);
            blockerUser.getFriends().remove(beingBlockedID);
            blockerUser.getBlockedList().add(beingBlockedID);

            Model beingBlockerUser = MainServer.getUsers().get(beingBlockedID);
            beingBlockerUser.getFriendRequests().remove(beingBlockedID);
            beingBlockerUser.getFriends().remove(beingBlockedID);

            return MainServer.updateDatabase(blockerUser) && MainServer.updateDatabase(beingBlockerUser);
        }
    }
}