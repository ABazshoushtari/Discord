package discord.signals;

import discord.client.Model;
import discord.mainServer.ClientHandler;
import discord.mainServer.MainServer;

import java.io.IOException;

public class LoginAction implements Action {
    private final String username;
    private final String password;

    public LoginAction(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public Object act() throws IOException {
        Integer myUID = MainServer.getIDs().get(username);
        if (myUID == null) {
            return null;
        }
        if (!MainServer.getUsers().containsKey(myUID)) {
            return null;
        } else if (!MainServer.getUsers().get(myUID).getPassword().equals(password)) {
            return null;
        } else {
            Model me = MainServer.getUsers().get(myUID);
            me.setStatus(me.getPreviousSetStatus());

            MainServer.getUsers().replace(myUID, me);
            MainServer.updateDatabase(me);

            ClientHandler.informRelatedPeople(me);

            return me;
        }
    }
}
