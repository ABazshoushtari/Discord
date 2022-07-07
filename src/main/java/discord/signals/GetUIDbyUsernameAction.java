package discord.signals;

import discord.mainServer.MainServer;

import java.io.IOException;

public class GetUIDbyUsernameAction implements Action{
    // Fields:
    private final String username;

    // Constructors:
    public GetUIDbyUsernameAction(String username) {
        this.username = username;
    }

    // Methods:
    @Override
    public Object act() throws IOException {
        return MainServer.getIDs().getOrDefault(username, null);
    }
}
