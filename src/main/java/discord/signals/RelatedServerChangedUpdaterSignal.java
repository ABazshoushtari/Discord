package discord.signals;

public class RelatedServerChangedUpdaterSignal extends UpdaterSignal {
    public RelatedServerChangedUpdaterSignal(Integer unicode) {
        ID = unicode;
    }
}
