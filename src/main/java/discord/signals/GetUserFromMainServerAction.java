package discord.signals;

import discord.mainServer.MainServer;

public class GetUserFromMainServerAction implements Action {
    private String username;
    private Integer UID;
    private boolean byUID;

    public GetUserFromMainServerAction(String username) {
        this.username = username;
        byUID = false;
    }

    public GetUserFromMainServerAction(Integer UID) {
        this.UID = UID;
        byUID = true;
    }

    @Override
    public Object act() {
        if (!byUID) {
            UID = MainServer.getIDs().getOrDefault(username, null);
            if (UID == null) {
                return null;
            }
        }
        return MainServer.getUsers().getOrDefault(UID, null);
    }
}
