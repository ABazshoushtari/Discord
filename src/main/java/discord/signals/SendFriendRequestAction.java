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
            return -1;
        }
        if (!MainServer.getUsers().containsKey(receiverUID)) {
            return -1;
        } else {
            Model receiverUser = MainServer.getUsers().get(receiverUID);
            int requesterUID = MainServer.getIDs().get(requester);
            if (receiverUser.getIncomingFriendRequests().contains(requesterUID)) {
                return -2;
            }
            if (receiverUser.getBlockedList().contains(requesterUID)) {
                return -3;
            }

            Model requesterUser = MainServer.getUsers().get(requesterUID);
            requesterUser.getSentFriendRequests().add(requesterUID);

            receiverUser.getIncomingFriendRequests().add(requesterUID);
            MainServer.updateDatabaseAndMainServer(receiverUser);

            for (ClientHandler ch : clientHandlers) {
                if (ch.getUser() != null) {
                    if (ch.getUser().getUID().equals(receiverUID)) {
                        ch.getMySocket().write(new FriendRequestModelUpdaterSignal(requesterUID));
                        break;
                    }
                }
            }

            return receiverUID;
        }
    }
}
