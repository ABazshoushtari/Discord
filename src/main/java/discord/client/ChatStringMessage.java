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
}
