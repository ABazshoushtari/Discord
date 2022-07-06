package discord.signals;

import discord.mainServer.MainServer;
import discord.client.Model;
import discord.client.Status;

public class LoginAction implements Action {
    private final String username;
    private final String password;

    public LoginAction(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public Object act() {
        Integer UID = MainServer.getIDs().get(username);
        if (UID == null) {
            return null;
        }
        if (!MainServer.getUsers().containsKey(UID)) {
            return null;
        } else if (!MainServer.getUsers().get(UID).getPassword().equals(password)) {
            return null;
        } else {
            Model user = MainServer.getUsers().get(UID);
            user.setStatus(Status.Online);
            return user;
        }
    }
}
