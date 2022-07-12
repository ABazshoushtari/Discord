package discord.client;

import discord.signals.Action;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;

public abstract class ChatMessage implements Action {
    // Fields:
    protected Integer senderUID;
    protected byte[] senderImage;
    protected String senderImageType;
    protected String senderUsername;
    protected Integer receiverUID;
    protected String dateTime;
    protected HashMap<Integer, HashSet<Reaction>> reactions;  //  maps UID of the person who has reacted to this message to its reactions
    protected boolean edited;

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

    public byte[] getSenderImage() {
        return senderImage;
    }

    public String getSenderImageType() {
        return senderImageType;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public Integer getSenderUID() {
        return senderUID;
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

    // Setter Methods:

    public void setSenderImage(byte[] senderImage) {
        this.senderImage = senderImage;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public void setSenderUID(Integer senderUID) {
        this.senderUID = senderUID;
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
}
