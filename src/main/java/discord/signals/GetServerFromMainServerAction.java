package discord.signals;

import discord.mainServer.MainServer;

public class GetServerFromMainServerAction implements Action {
    private final int unicode;

    public GetServerFromMainServerAction(int unicode) {
        this.unicode = unicode;
    }

    @Override
    public Object act() {
        return MainServer.getServers().getOrDefault(unicode, null);
    }
}
