package discord.signals;

import discord.mainServer.MainServer;

import java.io.IOException;

public class removeFriendFromServerAction implements Action {

    private final int serverUnicode;
    private final String beingRemovedMember;

    public removeFriendFromServerAction(int serverUnicode, String beingRemovedMember) {
        this.serverUnicode = serverUnicode;
        this.beingRemovedMember = beingRemovedMember;
    }

    @Override
    public Object act() throws IOException {
        MainServer.getServers().get(serverUnicode).getMembers().remove(beingRemovedMember);
        return MainServer.updateDatabase(MainServer.getServers().get(serverUnicode));
    }
}
