package discord.client;

import java.io.IOException;
import java.net.URL;

public class ChatURLMessage extends ChatMessage {
    // Fields:
//  message is  private String fileName;
    private URL url;

    // Constructors:
    public ChatURLMessage(Integer senderUID, Integer receiverUID, URL url) {
        super(senderUID, receiverUID);
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
