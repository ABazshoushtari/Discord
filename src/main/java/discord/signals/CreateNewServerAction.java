package discord.signals;

import discord.mainServer.MainServer;

public class CreateNewServerAction implements Action {
    @Override
    public Object act() {
        int unicode = 0;
        while (MainServer.getServers().containsKey(unicode)) {
            unicode++;
        }
        return unicode;
    }
}
