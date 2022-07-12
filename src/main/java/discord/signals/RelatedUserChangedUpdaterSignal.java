package discord.signals;

public class RelatedUserChangedUpdaterSignal extends UpdaterSignal {
    public RelatedUserChangedUpdaterSignal(Integer UID) {
        ID = UID;
    }
}
