package discord.signals;

//import discord.DownloadableFile;
import discord.mainServer.MainServer;
import discord.client.Model;

import java.net.URL;
import java.util.ArrayList;

public class CheckFriendRequestsAction implements Action {
    private final String username;
    private final int index;
    private final boolean accept;

    public CheckFriendRequestsAction(String username, int index, boolean accept) {
        this.username = username;
        this.index = index;
        this.accept = accept;
    }

    @Override
    public Object act() {
        if (!MainServer.getUsers().containsKey(username)) {
            return null;
        }
        Model user = MainServer.getUsers().get(username);
        String requesterUsername = user.getFriendRequests().get(index);
        Model requester = MainServer.getUsers().get(requesterUsername);
        boolean DBConnect = true;
        if (accept) {

            user.getFriends().add(requesterUsername);
            requester.getFriends().add(username);


            user.getIsInChat().put(requesterUsername, false);
            user.getPrivateChats().put(requesterUsername, new ArrayList<>());
            user.getUrlsOfPrivateChat().put(requesterUsername, new ArrayList<>());
            //user.getFilesOfPrivateChat().put(requesterUsername, new ArrayList<DownloadableFile>());

            requester.getIsInChat().put(username, false);
            requester.getPrivateChats().put(username, new ArrayList<>());
            requester.getUrlsOfPrivateChat().put(username, new ArrayList<>());
            //requester.getFilesOfPrivateChat().put(username, new ArrayList<DownloadableFile>());

            DBConnect = MainServer.updateDatabase(requester);
        }
        user.getFriendRequests().remove(index);
        DBConnect = DBConnect && MainServer.updateDatabase(user);
        return DBConnect;
    }
}
