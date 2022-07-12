package discord.signals;

import discord.client.Model;

public abstract class ModelUpdaterSignal extends UpdaterSignal {
    protected Model beingUpdatedModel;
    protected final Object beingChangedScreenElement;

    public ModelUpdaterSignal(Object beingChangedScreenElement) {
        this.beingChangedScreenElement = beingChangedScreenElement;
    }

    public void setBeingUpdatedModel(Model beingUpdatedModel) {
        this.beingUpdatedModel = beingUpdatedModel;
    }

    public abstract Model getUpdatedModel();

    public Object getBeingChangedScreenElement() {
        return beingChangedScreenElement;
    }
}
