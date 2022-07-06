package discord.client;

import javafx.scene.image.Image;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class Model implements Asset {
    // Fields:
    private final int UID;
    private byte[] avatarImage;
    private String contentType;  // type of avatarImage
    private String username;
    private String password;
    private String email;
    private String phoneNumber;
    private Status status;
    private Status previousSetStatus;
    private final LinkedList<Integer> friendRequests;
    private final LinkedList<Integer> friends;
    private final LinkedList<Integer> blockedList;
    private final HashMap<Integer, Boolean> isInChat;
    // maps all the friends' UIDs to whether this user is in their private char (true) or not (false)
    private final HashMap<Integer, ArrayList<String>> privateChats;
    // maps all the friend's UIDs to all the exchanged messages between this user and them
    private final HashMap<Integer, ArrayList<URL>> urlsOfPrivateChat;
    // maps all the friends UIDs to all the urls exchanged between then
    private final HashMap<Integer, ArrayList<DownloadableFile>> filesOfPrivateChat;
    // maps all the friends UIDs to all the downloaded files exchanged between then
    private final ArrayList<Integer> servers;
    // holds only the unicode of the servers this user is a part of

    // Constructors:
    public Model(int UID, String username, String password, String email, String phoneNumber) {
        this.UID = UID;
        this.avatarImage = null;
        this.username = username;
        this.password = password;
        this.email = email;
        this.phoneNumber = phoneNumber;
        status = null;
        previousSetStatus = null;
        friendRequests = new LinkedList<>();
        friends = new LinkedList<>();
        blockedList = new LinkedList<>();
        isInChat = new HashMap<>();
        privateChats = new HashMap<>();
        urlsOfPrivateChat = new HashMap<>();
        filesOfPrivateChat = new HashMap<>();
        servers = new ArrayList<>();
    }

    // Getters:
    public int getUID() {
        return UID;
    }

    public byte[] getAvatarImage() {
        return avatarImage;
    }

    public String getContentType() {
        return contentType;
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

    public LinkedList<Integer> getFriendRequests() {
        return friendRequests;
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

    public HashMap<Integer, ArrayList<String>> getPrivateChats() {
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

    public void setContentType(String contentType) {
        this.contentType = contentType;
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
    public String toString() {
        return username + " " + password + " " + email + " " + phoneNumber;
    }
}

