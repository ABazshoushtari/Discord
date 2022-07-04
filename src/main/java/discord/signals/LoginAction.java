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
        if (!MainServer.getUsers().containsKey(username)) {
            return null;
        } else if (!MainServer.getUsers().get(username).getPassword().equals(password)) {
            return null;
        } else {
            Model user = MainServer.getUsers().get(username);
            user.setStatus(Status.Online);
            return user;
        }
    }
}
