package discord.signals;

import discord.client.Model;
import discord.client.Server;
import discord.mainServer.ClientHandler;
import discord.mainServer.MainServer;

import java.io.IOException;

import static discord.mainServer.ClientHandler.clientHandlers;

public class RemoveMemberFromServerAction implements Action {

    private final int serverUnicode;
    private final Integer beingRemovedMember;

    public RemoveMemberFromServerAction(int serverUnicode, Integer beingRemovedMember) {
        this.serverUnicode = serverUnicode;
        this.beingRemovedMember = beingRemovedMember;
    }

    @Override
    public Object act() throws IOException {

        Server server = MainServer.getServers().get(serverUnicode);
        server.getMembers().remove(beingRemovedMember);

        Model beingRemovedUser = MainServer.getUsers().get(beingRemovedMember);
        beingRemovedUser.getServers().remove(serverUnicode);

        for (ClientHandler ch : clientHandlers) {
            if (ch.getUser() != null) {
                if (ch.getUser().getUID().equals(beingRemovedMember)) {
                    synchronized (ch.getMySocket()) {
                        ch.getMySocket().write(new GotRemovedFromAServerModelUpdaterSignal(server));
                    }
                    break;
                }
            }
        }

        MainServer.updateDatabase(server);
        return null;
    }
}
