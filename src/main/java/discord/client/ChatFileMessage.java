package discord.client;

import java.io.FileInputStream;
import java.io.IOException;

public class ChatFileMessage extends ChatMessage{
    // Fields:
//  message is  private String fileName;
    private byte[] bytes;

    // Constructors:
    public ChatFileMessage(Integer senderUID, Integer receiverUID, String fileName, FileInputStream fileInputStream) throws IOException {
        super(senderUID, receiverUID);
        message = fileName;
        bytes = fileInputStream.readAllBytes();
    }

    public ChatFileMessage(Integer senderUID, Integer receiverUID, String fileName, byte[] bytes) {
        super(senderUID, receiverUID);
        message = fileName;
        this.bytes = bytes;
    }

    // Methods:
    // Getters:
    public byte[] getBytes() {
        return bytes;
    }

    // Setters:
    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

}
