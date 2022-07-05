package discord.signals;

import discord.mainServer.MainServer;

public class GetUserFromMainServerAction implements Action {
    private final String username;

    public GetUserFromMainServerAction(String username) {
        this.username = username;
    }

    @Override
    public Object act() {
        Integer UID = MainServer.getIDs().getOrDefault(username, null);
        if (UID == null) {
            return null;
        }
        return MainServer.getUsers().getOrDefault(UID, null);
    }
}
