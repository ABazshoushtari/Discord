package discord.signals;

import discord.mainServer.MainServer;

import java.io.IOException;

public class GetUserRecordFromMainServer implements Action {

    private final Integer friendUID;
    private final Integer myUID;

    public GetUserRecordFromMainServer(Integer friendUID, Integer myUID) {
        this.friendUID = friendUID;
        this.myUID = myUID;
    }

    @Override
    public Object act() throws IOException {
        return MainServer.getUsers().get(friendUID).getFriendRecord(myUID);
    }
}
