package discord.signals;

import discord.client.Model;

public class RelatedUserChangedSignal extends ModelUpdaterSignal {

    private final Model relatedUserChanged;

    public RelatedUserChangedSignal(Model relatedUserChanged) {
        this.relatedUserChanged = relatedUserChanged;
    }

    @Override
    public Model getUpdatedModel() {
        return null;
    }

    public Model getRelatedUserChanged() {
        return relatedUserChanged;
    }
}
