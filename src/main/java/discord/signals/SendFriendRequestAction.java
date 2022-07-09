package discord.signals;

import discord.mainServer.ClientHandler;
import discord.mainServer.MainServer;
import discord.client.Model;

import java.io.IOException;

import static discord.mainServer.ClientHandler.clientHandlers;

public class SendFriendRequestAction implements Action {
    private final String requester;
    private final String username;

    public SendFriendRequestAction(String requester, String username) {
        this.requester = requester;
        this.username = username;
    }

    @Override
    public Object act() throws IOException {
        Integer receiverUID = MainServer.getIDs().getOrDefault(username, null);
        if (receiverUID == null) {
            return 0;
        }
        if (!MainServer.getUsers().containsKey(receiverUID)) {
            return 0;
        } else {
            Model user = MainServer.getUsers().get(receiverUID);
            int requesterUID = MainServer.getIDs().get(requester);
            if (user.getIncomingFriendRequests().contains(requesterUID)) {
                return 1;
            }
            if (user.getBlockedList().contains(requesterUID)) {
                return 2;
            }
            user.getIncomingFriendRequests().add(requesterUID);
            MainServer.updateDatabase(user);

            for (ClientHandler ch : clientHandlers) {
                if (ch.getUser() != null) {
                    if (ch.getUser().getUID().equals(receiverUID)) {
                        ch.getMySocket().write(new FriendRequestModelUpdaterSignal(requesterUID));
                        break;
                    }
                }
            }

            return 3;
        }
    }
}
