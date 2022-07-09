package discord.signals;

import discord.client.Status;
import discord.mainServer.ClientHandler;
import discord.mainServer.MainServer;
import discord.client.Model;

import java.io.IOException;

import static discord.mainServer.ClientHandler.clientHandlers;

public class UpdateUserOnMainServerAction implements Action {
    private final Model updatedMe;
    private String oldUsername;
    private final boolean usernameIsChanged;

    public UpdateUserOnMainServerAction(Model updatedMe, String oldUsername) {
        this.updatedMe = updatedMe;
        this.oldUsername = oldUsername;
        usernameIsChanged = true;
    }

    public UpdateUserOnMainServerAction(Model updatedMe) {
        this.updatedMe = updatedMe;
        usernameIsChanged = false;
    }

    @Override
    public Object act() throws IOException {

        if (usernameIsChanged) {
            MainServer.getIDs().remove(oldUsername);
            MainServer.getIDs().put(updatedMe.getUsername(), updatedMe.getUID());
        }

        for (ClientHandler ch : clientHandlers) {
            if (ch.getUser() != null) {
                if (updatedMe.getFriends().contains(ch.getUser().getUID())) {
                    ch.getMySocket().write(new FriendChangedSignal());
                }
            }
        }

        MainServer.getUsers().replace(updatedMe.getUID(), updatedMe);
        MainServer.updateDatabase(updatedMe);

        return updatedMe;
    }
}