package discord.signals;

public record FriendRequestSignal(int requesterUID) implements Signal {
}
