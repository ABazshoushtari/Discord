package discord.signals;

public record LostAFriendSignal(Integer removerUID) implements Signal {
}
