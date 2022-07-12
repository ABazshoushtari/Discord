package discord.signals;

import discord.mainServer.ClientHandler;
import discord.mainServer.MainServer;
import discord.client.Model;

import java.io.IOException;

import static discord.mainServer.ClientHandler.clientHandlers;

public class SendFriendRequestAction implements Action {
    private final Integer requesterUID;
    private final Integer receiverUID ;

    public SendFriendRequestAction(Integer requesterUID, Integer receiverUID) {
        this.requesterUID = requesterUID;
        this.receiverUID = receiverUID;
    }

    @Override
    public Object act() throws IOException {
        if (!MainServer.getUsers().containsKey(receiverUID)) {
            return 0;
        } else {

            Model receiverUser = MainServer.getUsers().get(receiverUID);

            if (receiverUser.getIncomingFriendRequests().contains(requesterUID)) {
                return 1;
            }
            if (receiverUser.getBlockedList().contains(requesterUID)) {
                return 2;
            }

            Model requesterUser = MainServer.getUsers().get(requesterUID);

            requesterUser.getSentFriendRequests().add(receiverUID);
            receiverUser.getIncomingFriendRequests().add(requesterUID);

            MainServer.updateDatabase(receiverUser);
            MainServer.updateDatabase(requesterUser);

            for (ClientHandler ch : clientHandlers) {
                if (ch.getUser() != null) {
                    if (ch.getUser().getUID().equals(receiverUID)) {
                        synchronized (ch.getMySocket()) {
                            ch.getMySocket().write(new FriendRequestModelUpdaterSignal(requesterUser));
                        }
                        break;
                    }
                }
            }

            return 3;
        }
    }
}
