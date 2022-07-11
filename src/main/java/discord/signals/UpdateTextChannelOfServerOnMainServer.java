package discord.signals;

import discord.client.Server;
import discord.client.TextChannel;
import discord.mainServer.MainServer;

import java.io.IOException;

public class UpdateTextChannelOfServerOnMainServer implements Action {
    private final int serverUnicode;
    private final int textChannelIndex;
    private final TextChannel updatedTextChannel;

    public UpdateTextChannelOfServerOnMainServer(int serverUnicode, int textChannelIndex, TextChannel updatedTextChannel) {
        this.serverUnicode = serverUnicode;
        this.textChannelIndex = textChannelIndex;
        this.updatedTextChannel = updatedTextChannel;
    }

    @Override
    public Object act() throws IOException {
        synchronized (MainServer.getServers().get(serverUnicode).getTextChannels().get(textChannelIndex)) {
            Server server = MainServer.getServers().get(serverUnicode);
            server.getTextChannels().set(textChannelIndex, updatedTextChannel);

            //MainServer.getServers().replace(serverUnicode, server);
            MainServer.updateDatabase(server);
            return null;
        }
    }
}