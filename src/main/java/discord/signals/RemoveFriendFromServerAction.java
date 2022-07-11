package discord.signals;

import discord.client.Server;
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
        Server server = MainServer.getServers().get(serverUnicode);
        server.getMembers().remove(beingRemovedMemberID);

        MainServer.getServers().replace(serverUnicode, server);
        MainServer.updateDatabase(server);
        return null;
    }
}
