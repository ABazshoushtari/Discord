package discord.signals;

import discord.client.Model;

public class RelatedUserChangedUpdaterSignal extends UpdaterSignal {
    private final Model relatedUserChanged;

    public RelatedUserChangedUpdaterSignal(Model relatedUserChanged) {
        this.relatedUserChanged = relatedUserChanged;
    }

    public Model getRelatedUserChanged() {
        return relatedUserChanged;
    }
}
