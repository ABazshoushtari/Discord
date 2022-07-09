package discord.signals;

import discord.client.Model;

import java.io.Serializable;

public abstract class ModelUpdaterSignal implements Serializable {
    protected Model beingUpdatedModel;

    public void setBeingUpdatedModel(Model beingUpdatedModel) {
        this.beingUpdatedModel = beingUpdatedModel;
    }

    public abstract Model getUpdatedModel();
}
