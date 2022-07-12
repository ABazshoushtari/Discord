package discord.client;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class Model implements Asset {
    // Fields:
    private final Integer UID;
    private byte[] avatarImage;
    private String avatarContentType;  // type of avatarImage
    private String username;
    private String password;
    private String email;
    private String phoneNumber;
    private Status status;
    private Status previousSetStatus;
    private final LinkedList<Integer> incomingFriendRequests;
    private final LinkedList<Integer> sentFriendRequests;
    private final LinkedList<Integer> friends;
    private final LinkedList<Integer> blockedList;
    private final HashMap<Integer, Boolean> isInChat;
    // maps all the friends' UIDs to whether this user is in their private char (true) or not (false)
    private final HashMap<Integer, ArrayList<ChatMessage>> privateChats;
    // maps all the friend's UIDs to all the exchanged messages between this user and them
    private final HashMap<Integer, ArrayList<URL>> urlsOfPrivateChat;
    // maps all the friends UIDs to all the urls exchanged between then
    private final HashMap<Integer, ArrayList<DownloadableFile>> filesOfPrivateChat;
    // maps all the friends UIDs to all the downloaded files exchanged between then
    private final ArrayList<Integer> servers;
    // holds only the unicode of the servers this user is a part of

    // Constructors:
    public Model(Integer UID, String username, String password, String email, String phoneNumber) {
        this.UID = UID;
        this.avatarImage = null;
        this.username = username;
        this.password = password;
        this.email = email;
        this.phoneNumber = phoneNumber;
        status = Status.Online;
        previousSetStatus = Status.Online;
        incomingFriendRequests = new LinkedList<>();
        sentFriendRequests = new LinkedList<>();
        friends = new LinkedList<>();
        blockedList = new LinkedList<>();
        isInChat = new HashMap<>();
        privateChats = new HashMap<>();
        urlsOfPrivateChat = new HashMap<>();
        filesOfPrivateChat = new HashMap<>();
        servers = new ArrayList<>();
    }

    // Getters:
    public Integer getUID() {
        return UID;
    }

    public Integer getID() {
        return UID;
    }

    public byte[] getAvatarImage() {
        return avatarImage;
    }

    public String getAvatarContentType() {
        return avatarContentType;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Status getStatus() {
        return status;
    }

    public Status getPreviousSetStatus() {
        return previousSetStatus;
    }

    public LinkedList<Integer> getIncomingFriendRequests() {
        return incomingFriendRequests;
    }

    public LinkedList<Integer> getSentFriendRequests() {
        return sentFriendRequests;
    }

    public LinkedList<Integer> getFriends() {
        return friends;
    }

    public LinkedList<Integer> getBlockedList() {
        return blockedList;
    }

    public HashMap<Integer, Boolean> getIsInChat() {
        return isInChat;
    }

    public HashMap<Integer, ArrayList<ChatMessage>> getPrivateChats() {
        return privateChats;
    }

    public HashMap<Integer, ArrayList<URL>> getUrlsOfPrivateChat() {
        return urlsOfPrivateChat;
    }

    public HashMap<Integer, ArrayList<DownloadableFile>> getFilesOfPrivateChat() {
        return filesOfPrivateChat;
    }

    public ArrayList<Integer> getServers() {
        return servers;
    }

    // Setters:
    public void setAvatarImage(byte[] avatarImage) {
        this.avatarImage = avatarImage;
    }

    public void setAvatarContentType(String avatarContentType) {
        this.avatarContentType = avatarContentType;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setPreviousSetStatus(Status previousSetStatus) {
        this.previousSetStatus = previousSetStatus;
    }

    // Other Methods:
    public void addFriend(Integer newFriendUID) {
        friends.add(newFriendUID);
        isInChat.put(newFriendUID, false);
        privateChats.put(newFriendUID, new ArrayList<>());
        urlsOfPrivateChat.put(newFriendUID, new ArrayList<>());
        filesOfPrivateChat.put(newFriendUID, new ArrayList<>());
    }

    public void removeFriend(Integer lostFriendUID) {
        friends.remove(lostFriendUID);
        isInChat.remove(lostFriendUID);
        privateChats.remove(lostFriendUID);
        urlsOfPrivateChat.remove(lostFriendUID);
        filesOfPrivateChat.remove(lostFriendUID);
    }

    public void enterPrivateChat(Integer friendUID) {
        for (Integer current : isInChat.keySet()) {
            if (current.equals(friendUID)) {
                isInChat.replace(current, true);
                continue;
            }
            isInChat.replace(current, false);  // make other isInChats false
        }
    }

    public void makeAllIsInChatsFalse() {
        for (Integer current : isInChat.keySet()) {
            isInChat.replace(current, false);
        }
    }

    public String toString() {
        return username + " " + password + " " + email + " " + phoneNumber;
    }

    public FriendRecord getFriendRecord(Integer friendUID) {
        return new FriendRecord(avatarImage, avatarContentType, username, email, phoneNumber, status,
                privateChats.get(friendUID), urlsOfPrivateChat.get(friendUID), filesOfPrivateChat.get(friendUID));
    }
}

