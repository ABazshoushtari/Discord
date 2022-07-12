package discord.client;

import discord.mainServer.ClientHandler;
import discord.mainServer.MainServer;
import discord.signals.Action;
import discord.signals.ChatMessageSignal;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;

import static discord.mainServer.ClientHandler.clientHandlers;

public abstract class ChatMessage implements Action {
    // Fields:
    protected Integer senderUID;
    protected byte[] senderImage;
    protected String senderUsername;
    protected Integer receiverUID;
    protected String dateTime;
    protected HashMap<Integer, HashSet<Reaction>> reactions;  //  maps UID of the person who has reacted to this message to its reactions
    protected boolean edited;

    protected String message = ""; // a message or fileName

    // Constructors:
    public ChatMessage(Integer senderUID, Integer receiverUID) {
        this.senderUID = senderUID;
        this.receiverUID = receiverUID;
        dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm"));
        reactions = new HashMap<>();
        edited = false;
    }

    // Methods:
    // Getter Methods:

    public Integer getSenderUID() {
        return senderUID;
    }

    public byte[] getSenderImage() {
        return senderImage;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public Integer getReceiverUID() {
        return receiverUID;
    }

    public String getDateTime() {
        return dateTime;
    }

    public HashMap<Integer, HashSet<Reaction>> getReactions() {
        return reactions;
    }

    public boolean isEdited() {
        return edited;
    }

    public String getMessage() {
        return message;
    }

    // Setter Methods:

    public void setSenderUID(Integer senderUID) {
        this.senderUID = senderUID;
    }

    public void setSenderImage(byte[] senderImage) {
        this.senderImage = senderImage;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public void setReceiverUID(Integer receiverUID) {
        this.receiverUID = receiverUID;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public void setReactions(HashMap<Integer, HashSet<Reaction>> reactions) {
        this.reactions = reactions;
    }

    public void setEdited(boolean edited) {
        this.edited = edited;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    // Other Methods:
    public void like(Integer UID) {
        if (!reactions.containsKey(UID)) {
            reactions.put(UID, new HashSet<>());
        }
        reactions.get(UID).add(Reaction.LIKE);
        System.out.println("liked");
    }

    public void dislike(Integer UID) {
        if (!reactions.containsKey(UID)) {
            reactions.put(UID, new HashSet<>());
        }
        reactions.get(UID).add(Reaction.DISLIKE);
        System.out.println("disliked");
    }

    public void laugh(Integer UID) {
        if (!reactions.containsKey(UID)) {
            reactions.put(UID, new HashSet<>());
        }
        reactions.get(UID).add(Reaction.LAUGH);
        System.out.println("laughed");
    }

    @Override
    public Object act() throws IOException {

        Model senderUser = MainServer.getUsers().get(senderUID);
        Model receiverUser = MainServer.getUsers().get(receiverUID);

        senderUsername = senderUser.getUsername();
        senderImage = senderUser.getAvatarImage();

        senderUser.getPrivateChats().get(receiverUID).add(this);
        receiverUser.getPrivateChats().get(senderUID).add(this);

        MainServer.updateDatabase(senderUser);
        MainServer.updateDatabase(receiverUser);

        for (ClientHandler ch : clientHandlers) {
            Model user = ch.getUser();
            if (user != null) {
                if (receiverUID.equals(user.getUID())) {

                    user = MainServer.getUsers().get(receiverUID);  //userOfClientHandler.getChangerUserUID()

                    if (user.getIsInChat().get(senderUID)) {
                        synchronized (ch.getMySocket()) {
                            ch.getMySocket().write(new ChatMessageSignal(this));
                            break;
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return message;
    }
}
