package discord.client;

import discord.mainServer.ClientHandler;
import discord.mainServer.MainServer;
import discord.signals.ChatMessageSignal;

import java.io.IOException;

import static discord.mainServer.ClientHandler.clientHandlers;

public class ChatStringMessage extends ChatMessage {
    // Fields:
    /* inherited fields:
    protected Integer senderUID;
    protected byte[] senderImage;
    protected String senderUsername;
    protected Integer receiverUID;
    protected String dateTime;
    protected HashMap<Integer, HashSet<Reaction>> reactions;  //  maps UID of the person who reacted to this message to its reactions
    protected String message;
     */

    // Constructors:
    public ChatStringMessage(Integer senderUID, Integer receiverUID, String message) {
        super(senderUID, receiverUID);
        this.message = message;
    }

    // Methods:

}
