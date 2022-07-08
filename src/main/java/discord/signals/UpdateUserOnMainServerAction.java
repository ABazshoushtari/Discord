package discord.signals;

import discord.client.Status;
import discord.mainServer.MainServer;
import discord.client.Model;

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
    public Object act() {
        if (usernameIsChanged) {
            MainServer.getIDs().remove(oldUsername);
            MainServer.getIDs().put(updatedMe.getUsername(), updatedMe.getUID());
        }
        MainServer.getUsers().replace(updatedMe.getUID(), updatedMe);
        if (MainServer.updateDatabase(updatedMe)) {
            return updatedMe;
        }
        return null;
    }
}