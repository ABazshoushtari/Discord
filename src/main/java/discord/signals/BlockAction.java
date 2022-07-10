package discord.signals;

import discord.client.Model;
import discord.mainServer.MainServer;

public class BlockAction implements Action {
    private final Integer blockerUID;
    private final Integer beingBlockedUID;

    public BlockAction(Integer blockerUID, Integer beingBlockedUID) {
        this.blockerUID = blockerUID;
        this.beingBlockedUID = beingBlockedUID;
    }

    @Override
    public Object act() {

        Model blockerUser = MainServer.getUsers().get(blockerUID);
        blockerUser.getIncomingFriendRequests().remove(beingBlockedUID);
        blockerUser.getFriends().remove(beingBlockedUID);
        blockerUser.getBlockedList().add(beingBlockedUID);

        Model beingBlockerUser = MainServer.getUsers().get(beingBlockedUID);
        beingBlockerUser.getIncomingFriendRequests().remove(beingBlockedUID);
        beingBlockerUser.getFriends().remove(beingBlockedUID);

        MainServer.updateDatabaseAndMainServer(blockerUser);
        MainServer.updateDatabaseAndMainServer(beingBlockerUser);

        return null;
    }
}