package discord.client;

public class ChatStringMessage extends ChatMessage {
    // Fields:
    /* inherited fields:
    protected Integer senderUID;
    protected Integer receiverUID;
    protected String dateTime;
    protected HashMap<Integer, HashSet<Reaction>> reactions;  //  maps UID of the person who reacted to this message to its reactions
     */
    private String message;
    // Constructors:
    public ChatStringMessage(Integer senderUID, Integer receiverUID, String message) {
        super(senderUID, receiverUID);
        this.message = message;
    }

    // Methods:
    // Getter Methods:
    public String getMessage() {
        return message;
    }

    // Setter Methods:
    public void setMessage(String message) {
        this.message = message;
    }

    // Other Methods:
}
