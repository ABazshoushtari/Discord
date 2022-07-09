package discord.signals;

public record AcceptFriendRequestSignal(Integer accepterUID) implements Signal {
}
