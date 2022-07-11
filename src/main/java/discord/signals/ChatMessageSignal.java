package discord.signals;

import discord.client.ChatMessage;
import discord.client.Model;

public class ChatMessageSignal extends ModelUpdaterSignal {

    private final ChatMessage chatMessage;

    public ChatMessageSignal(ChatMessage chatMessage) {
        this.chatMessage = chatMessage;
    }

    @Override
    public Model getUpdatedModel() {
        beingUpdatedModel.getPrivateChats().get(chatMessage.getSenderUID()).add(chatMessage);
        return beingUpdatedModel;
    }
}
