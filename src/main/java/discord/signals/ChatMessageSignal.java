package discord.signals;

import discord.client.ChatMessage;
import discord.client.Model;

public class ChatMessageSignal extends ModelUpdaterSignal {

    //private final ChatMessage chatMessage;

    public ChatMessageSignal(ChatMessage chatMessage) {
        super(chatMessage);
        //this.chatMessage = chatMessage;
    }

    @Override
    public Model getUpdatedModel() {

        beingUpdatedModel.getPrivateChats().get(((ChatMessage) beingChangedScreenElement).getSenderUID()).add((ChatMessage) beingChangedScreenElement);
        return beingUpdatedModel;
    }

//    public ChatMessage getChatMessage() {
//        return chatMessage;
//    }
}
