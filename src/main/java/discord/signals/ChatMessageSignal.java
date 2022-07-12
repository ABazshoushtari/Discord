package discord.signals;

import discord.client.ChatMessage;
import discord.client.Model;

public class ChatMessageSignal extends ModelUpdaterSignal {

    //private final ChatMessage chatMessage;
    private final boolean isTextChannelMessaage;

    public ChatMessageSignal(ChatMessage chatMessage, boolean isTextChannelMessaage) {
        super(chatMessage);
        this.isTextChannelMessaage = isTextChannelMessaage;
        //this.chatMessage = chatMessage;
    }

    @Override
    public Model getUpdatedModel() {
        if (!isTextChannelMessaage) {
            beingUpdatedModel.getPrivateChats().get(((ChatMessage) beingChangedScreenElement).getSenderUID()).add((ChatMessage) beingChangedScreenElement);
        }
        return beingUpdatedModel;
    }

//    public ChatMessage getChatMessage() {
//        return chatMessage;
//    }

    public boolean isTextChannelMessaage() {
        return isTextChannelMessaage;
    }
}
