package discord.signals;

import discord.mainServer.ClientHandler;
import discord.mainServer.MainServer;
import discord.client.Model;

import java.io.IOException;

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

        ClientHandler.informRelatedPeople(updatedMe);

        MainServer.getUsers().replace(updatedMe.getID(), updatedMe);
        MainServer.updateDatabase(updatedMe);

        return null;
    }
}