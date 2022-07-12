package discord.client;

import java.net.URL;
import java.util.ArrayList;

public record FriendRecord(byte[] avatarImage, String avatarContentType, String username, String email,
                           String phoneNumber, Status status, ArrayList<ChatMessage> thePrivateChat,
                           ArrayList<URL> theURLsOfThePrivateChat,
                           ArrayList<DownloadableFile> theFilesOfThePrivateChat) {

}
