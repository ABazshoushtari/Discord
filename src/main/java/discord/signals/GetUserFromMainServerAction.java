package discord.signals;

import discord.mainServer.MainServer;

public class GetUserFromMainServerAction implements Action {
    private final Integer UID;

    public GetUserFromMainServerAction(Integer UID) {
        this.UID = UID;
    }

    @Override
    public Object act() {
        return MainServer.getUsers().getOrDefault(UID, null);
    }
}
