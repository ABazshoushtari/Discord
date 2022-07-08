package discord.signals;

public class AcceptFriendRequestSignal {

    private final Integer accepterUID;

    public AcceptFriendRequestSignal(Integer accepterUID) {
        this.accepterUID = accepterUID;
    }
}
