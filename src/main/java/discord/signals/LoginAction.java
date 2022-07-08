package discord.signals;

import discord.client.Model;
import discord.mainServer.MainServer;

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
            Model me = MainServer.getUsers().get(UID);
            me.setStatus(me.getPreviousSetStatus());
            MainServer.getUsers().replace(UID, me);
            boolean DBConnect = MainServer.updateDatabase(me);
            return me;
        }
    }
}
