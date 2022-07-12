package discord.client;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class ChatURLMessage extends ChatMessage {
    // Fields:
//  message is  private String fileName;
    private URL url;

    // Constructors:
//    public ChatURLMessage(Integer senderUID, Integer receiverUID, URL url) {
//        super(senderUID, receiverUID);
//        this.url = url;
//        String parts[] = url.getFile().split("/");
//        message = "ULR_" + parts[parts.length - 1];
//    }

    public ChatURLMessage(Integer senderUID, ArrayList<Integer> receiverUIDs, int serverUnicode, int textChannelIndex, boolean isTextChannelMessage, URL url) {
        super(senderUID, receiverUIDs, serverUnicode, textChannelIndex, isTextChannelMessage);
        this.url = url;
        String parts[] = url.getFile().split("/");
        message = "ULR_" + parts[parts.length - 1];
    }

    // Getters:
    public URL getUrl() {
        return url;
    }

    // Setters:
    public void setUrl(URL url) {
        this.url = url;
    }

}
