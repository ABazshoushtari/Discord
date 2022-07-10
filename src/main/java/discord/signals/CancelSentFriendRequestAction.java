package discord.signals;

import discord.client.Model;
import discord.mainServer.ClientHandler;
import discord.mainServer.MainServer;

import java.io.IOException;

import static discord.mainServer.ClientHandler.clientHandlers;

public class CancelSentFriendRequestAction implements Action{

    private final Integer cancellerUID;
    private final Integer beingCanceledUID;

    public CancelSentFriendRequestAction(Integer cancellerUID, Integer beingCanceledUID) {
        this.cancellerUID = cancellerUID;
        this.beingCanceledUID = beingCanceledUID;
    }

    @Override
    public Object act() throws IOException {

        Model canceller = MainServer.getUsers().get(cancellerUID);
        canceller.getSentFriendRequests().remove(beingCanceledUID);

        MainServer.updateDatabaseAndMainServer(canceller);

        Model beingCancelled = MainServer.getUsers().get(beingCanceledUID);
        beingCancelled.getIncomingFriendRequests().remove(cancellerUID);

        MainServer.updateDatabaseAndMainServer(beingCancelled);

        for (ClientHandler ch : clientHandlers) {
            if (ch.getUser() != null) {
                if (ch.getUser().getUID().equals(beingCanceledUID)) {
                    ch.getMySocket().write(new CancelFriendRequestModelUpdaterSignal(cancellerUID));
                    break;
                }
            }
        }

        return canceller;
    }
}
