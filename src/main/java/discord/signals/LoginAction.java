package discord.signals;

import discord.client.Model;
import discord.mainServer.ClientHandler;
import discord.mainServer.MainServer;

import java.io.IOException;

import static discord.mainServer.ClientHandler.clientHandlers;

public class LoginAction implements Action {
    private final String username;
    private final String password;

    public LoginAction(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public Object act() throws IOException {
        Integer UID = MainServer.getIDs().get(username);
        if (UID == null) {
            return null;
        }
        if (!MainServer.getUsers().containsKey(UID)) {
            return null;
        } else if (!MainServer.getUsers().get(UID).getPassword().equals(password)) {
            return null;
        } else {
            Model me = MainServer.getUsers().get(UID);
            me.setStatus(me.getPreviousSetStatus());
            MainServer.getUsers().replace(UID, me);
            MainServer.updateDatabase(me);

            for (ClientHandler ch : clientHandlers) {
                if (ch.getUser() != null) {
                    if (ch.getUser().getFriends().contains(me.getUID())) {
                        ch.getMySocket().write(new FriendChangedSignal());
                    }
                }
            }

            return me;
        }
    }
}
