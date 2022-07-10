package discord.signals;

import discord.client.DownloadableFile;
import discord.client.DownloadURL;
import discord.mainServer.*;
import discord.client.Model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import static discord.mainServer.ClientHandler.clientHandlers;

public class PrivateChatAction implements Action {
    private final String sender;
    private String message;
    private final String receiver;

    public PrivateChatAction(String sender, String message, String receiver) {
        this.sender = sender;
        this.message = message;
        this.receiver = receiver;
    }

    @Override
    public Object act() throws IOException {

        int senderID = MainServer.getIDs().get(sender);
        int receiverID = MainServer.getIDs().get(receiver);

        Model senderUser = MainServer.getUsers().get(senderID);
        Model receiverUser = MainServer.getUsers().get(receiverID);

        if (!message.equalsIgnoreCase("#exit")) {
            if (message.startsWith("/") || message.startsWith("#")) {
                if (message.equalsIgnoreCase("#urls")) {
                    return showList(senderUser.getUrlsOfPrivateChat().get(receiverID));
                }
                if (message.equalsIgnoreCase("#files")) {
                    return showList(senderUser.getFilesOfPrivateChat().get(receiverID));
                }
                if (message.equalsIgnoreCase("#help")) {
                    return helpMenu();
                }
                if (message.startsWith("/file ")) {
                    String directory = message.substring(6);
                    File file = new File(directory);
                    if (!file.exists() && !file.isFile()) {
                        return "WARNING: Invalid format. Enter a file's directory which exists";
                    }
                    FileInputStream fileInputStream = new FileInputStream(file);
                    senderUser.getFilesOfPrivateChat().get(receiverID).add(new DownloadableFile(file.getName(), fileInputStream));
                    fileInputStream.close();

                    fileInputStream = new FileInputStream(file);
                    receiverUser.getFilesOfPrivateChat().get(senderID).add(new DownloadableFile(file.getName(), fileInputStream));
                    fileInputStream.close();

                    message = sender + ": uploaded " + file.getName();
                } else {
                    String[] command = message.split(" ");
                    if (command.length > 1) {
                        switch (command[0].toLowerCase()) {
                            case "/url" -> {
                                URL url;
                                try {
                                    url = new URL(command[1]);
                                } catch (MalformedURLException e) {
                                    return "WARNING: Invalid format. Enter a URL";
                                }
                                senderUser.getUrlsOfPrivateChat().get(receiverID).add(url);
                                receiverUser.getUrlsOfPrivateChat().get(senderID).add(url);

                                message = command[1];
                            }

                            case "#file" -> {
                                int fileIndex = checkIndex(senderUser.getFilesOfPrivateChat().get(receiverID), command[1]);
                                if (fileIndex == -1) {
                                    return "WARNING: Invalid format";
                                } else if (fileIndex == -2) {
                                    return "WARNING: invalid index. out of boundary";
                                } else {
                                    // return the DownloadableFile
                                    if (command.length >= 3) {  // download the file with a different name
                                        return new DownloadableFile(command[2], senderUser.getFilesOfPrivateChat().get(receiverID).get(fileIndex).getBytes());
                                    } else {
                                        return senderUser.getFilesOfPrivateChat().get(receiverID).get(fileIndex);
                                    }
                                }
                            }
                            case "#url" -> {
                                int urlIndex = checkIndex(senderUser.getUrlsOfPrivateChat().get(receiverID), command[1]);
                                if (urlIndex == -1) {
                                    return "WARNING: Invalid format";
                                } else if (urlIndex == -2) {
                                    return "WARNING: invalid index. out of boundary";
                                } else {
                                    return new DownloadURL(command[2], senderUser.getUrlsOfPrivateChat().get(receiverID).get(urlIndex));  //returns DownloadURL for downloading the URL
                                }
                            }
                        }
                    }
                }
            }

            if (!message.startsWith(sender)) {
                message = sender + ": " + message;
            }
            // updating database and server
//            senderUser.getPrivateChats().get(receiverID).add(message);
//            receiverUser.getPrivateChats().get(senderID).add(message);

            MainServer.updateDatabaseAndMainServer(senderUser);
            MainServer.updateDatabaseAndMainServer(receiverUser);

            // sending message from socket if the receiver is online and in the private chat
            for (ClientHandler c : clientHandlers) {
                Model userOfClientHandler = c.getUser();
                if (userOfClientHandler != null) {
                    if (receiver.equals(userOfClientHandler.getUsername())) {
                        userOfClientHandler = MainServer.getUsers().get(userOfClientHandler.getUID());
                        if (userOfClientHandler.getIsInChat().get(senderID)) {
                            c.getMySocket().write(message); // we can also write "this" object
                            return true;  //seen by the receiver in the moment
                        }
                    }
                }
            }
            return false;  //receiver is not currently in the chat
        } else {
            return senderUser;
        }

    }

    private <Type> int checkIndex(ArrayList<Type> arrayList, String message) {
        try {
            int index = Integer.parseInt(message) - 1;
            if (index >= 0 && index < arrayList.size()) {
                return index;
            } else {
                return -2;  // out of boundary
            }
        } catch (NumberFormatException e) {
            return -1;  // wrong format
        }
    }

    private <Type> String showList(ArrayList<Type> list) {  // in our program list is a list of URLs or DownloadableFiles
        String output = "";
        for (int i = 0; i < list.size(); ++i) {
            output = output.concat((i + 1) + ". " + list.get(i).toString() + "\n");
        }
        if (output.length() == 0) {
            output = "The list is empty";
        }
        return output;
    }

    private String helpMenu() {
        return """
                /url <URL_Link> -> to send link of a file so others can download it
                /file <file_address> -> to upload a file from your pc
                #urls -> prints list of all URLs in the chat
                #files -> prints list of all files in the chat
                #url <index> <target_file_name> -> download the specified url and save it with <target_file_name> name
                #file <index> -> download the specified file and save it without changing the original name of the file
                #file <index> <target_file_name> -> download the specified file and save it with <target_file_name> name
                """;
    }
}