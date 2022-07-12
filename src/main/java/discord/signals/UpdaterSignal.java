package discord.signals;

import java.io.Serializable;

public abstract class UpdaterSignal implements Serializable {

    protected Integer ID;

    public Integer getID() {
        return ID;
    }
}
