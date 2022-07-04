package discord.client;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class Model implements Asset {
    // Fields:
    private final String username;
    private String password;
    private String email;
    private String phoneNumber;
    private Status status;
    private final LinkedList<String> friendRequests;
    private final LinkedList<String> friends;
    private final LinkedList<String> blockedList;
    private final HashMap<String, Boolean> isInChat;
    // maps all the friends' usernames to whether this user is in their private char (true) or not (false)
    private final HashMap<String, ArrayList<String>> privateChats;
    // maps all the friend's usernames to all the exchanged messages between this user and them
    private final HashMap<String, ArrayList<URL>> urlsOfPrivateChat;
    private final HashMap<String, ArrayList<DownloadableFile>> filesOfPrivateChat;
    private final ArrayList<Integer> servers;
    // holds only the unicode of the servers this user is a part of

    // Constructors:
    public Model(String username, String password, String email, String phoneNumber) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.phoneNumber = phoneNumber;
        status = Status.Offline;
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

    public LinkedList<String> getFriendRequests() {
        return friendRequests;
    }

    public LinkedList<String> getFriends() {
        return friends;
    }

    public LinkedList<String> getBlockedList() {
        return blockedList;
    }

    public HashMap<String, Boolean> getIsInChat() {
        return isInChat;
    }

    public HashMap<String, ArrayList<String>> getPrivateChats() {
        return privateChats;
    }

    public HashMap<String, ArrayList<URL>> getUrlsOfPrivateChat() {
        return urlsOfPrivateChat;
    }

    public HashMap<String, ArrayList<DownloadableFile>> getFilesOfPrivateChat() {
        return filesOfPrivateChat;
    }

    public ArrayList<Integer> getServers() {
        return servers;
    }

    // Setters:
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

    // Other Methods:
    public String toString() {
        return username + " " + password + " " + email + " " + phoneNumber;
    }
}

