package discord.signals;

public class AServerIsChangedSignal extends UpdaterSignal {
    public AServerIsChangedSignal(Integer unicode) {
        ID = unicode;
    }
}
