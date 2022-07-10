package discord.signals;

import discord.mainServer.MainServer;

import java.io.IOException;

public class RemoveFriendFromServerAction implements Action {

    private final int serverUnicode;
    private final String beingRemovedMember;

    public RemoveFriendFromServerAction(int serverUnicode, String beingRemovedMember) {
        this.serverUnicode = serverUnicode;
        this.beingRemovedMember = beingRemovedMember;
    }

    @Override
    public Object act() throws IOException {
        int beingRemovedMemberID = MainServer.getIDs().get(beingRemovedMember);
        MainServer.getServers().get(serverUnicode).getMembers().remove(beingRemovedMemberID);
        MainServer.updateDatabaseAndMainServer(MainServer.getServers().get(serverUnicode));
        return null;
    }
}
