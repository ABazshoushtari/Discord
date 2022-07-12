package discord.signals;

import discord.client.Model;

public abstract class ModelUpdaterSignal extends UpdaterSignal {
    protected Model beingUpdatedModel;

    public void setBeingUpdatedModel(Model beingUpdatedModel) {
        this.beingUpdatedModel = beingUpdatedModel;
    }

    public abstract Model getUpdatedModel();
}
