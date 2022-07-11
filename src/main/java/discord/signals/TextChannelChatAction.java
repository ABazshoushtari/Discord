package discord.signals;

import discord.client.*;
import discord.mainServer.ClientHandler;
import discord.mainServer.MainServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import static discord.mainServer.ClientHandler.clientHandlers;

public class TextChannelChatAction implements Action {
    private final String sender;
    private String message;
    private final int serverUnicode;
    private final int textChannelIndex;
    private final ArrayList<Integer> receivers;  // holds the UID all the members of the textChannel except the sender

    public TextChannelChatAction(String sender, String message, int serverUnicode, int textChannelIndex, ArrayList<Integer> receivers) {
        this.sender = sender;
        message = message.trim();
        this.message = message;
        this.serverUnicode = serverUnicode;
        this.textChannelIndex = textChannelIndex;
        this.receivers = receivers;
    }

    @Override
    public Object act() throws IOException {
        int senderID = MainServer.getIDs().get(sender);
        Model senderUser = MainServer.getUsers().get(senderID);
        if (!message.equalsIgnoreCase("#exit")) {
            if (message.startsWith("/") || message.startsWith("#")) {
                if (message.equalsIgnoreCase("#pin")) {
                    return "PINNED MESSAGE: " + MainServer.getServers().get(serverUnicode).getTextChannels().get(textChannelIndex).getPinnedMessage();
                }
                if (message.equalsIgnoreCase("#urls")) {
                    return showList(MainServer.getServers().get(serverUnicode).getTextChannels().get(textChannelIndex).getUrls());
                }
                if (message.equalsIgnoreCase("#files")) {
                    return showList(MainServer.getServers().get(serverUnicode).getTextChannels().get(textChannelIndex).getFiles());
                }
                if (message.equalsIgnoreCase("#help")) {
                    return helpMenu();
                }

                // upload file
                if (message.startsWith("/file ")) {
                    String directory = message.substring(6);
                    File file = new File(directory);
                    if (!file.exists() && !file.isFile()) {
                        return "WARNING: Invalid format. Enter a file's directory which exists";
                    }
                    FileInputStream fileInputStream = new FileInputStream(file);
                    MainServer.getServers().get(serverUnicode).getTextChannels().get(textChannelIndex).getFiles().add(new DownloadableFile(file.getName(), fileInputStream));
                    fileInputStream.close();

                    message = "uploaded " + file.getName();
                } else {
                    String[] command = message.split(" ");
                    switch (command[0].toLowerCase()) {
                        //download file
                        case "#url" -> {
                            int urlIndex = checkIndex(MainServer.getServers().get(serverUnicode).getTextChannels().get(textChannelIndex).getUrls(), command[1]);
                            if (urlIndex == -1 || command.length != 3) {
                                return "WARNING: Invalid format";
                            } else if (urlIndex == -2) {
                                return "WARNING: invalid index. out of boundary";
                            } else {
                                //returns DownloadURL for downloading the URL
                                return new DownloadURL(command[2], MainServer.getServers().get(serverUnicode).getTextChannels().get(textChannelIndex).getUrls().get(urlIndex));
                            }
                        }
                        case "#file" -> {
                            int fileIndex = checkIndex(MainServer.getServers().get(serverUnicode).getTextChannels().get(textChannelIndex).getFiles(), command[1]);
                            if (fileIndex == -1) {
                                return "WARNING: Invalid format";
                            } else if (fileIndex == -2) {
                                return "WARNING: invalid index. out of boundary";
                            } else {
                                // return the DownloadableFile
                                if (command.length >= 3) {
                                    return new DownloadableFile(command[2], MainServer.getServers().get(serverUnicode).getTextChannels().get(textChannelIndex).getFiles().get(fileIndex).getBytes());
                                } else {
                                    return MainServer.getServers().get(serverUnicode).getTextChannels().get(textChannelIndex).getFiles().get(fileIndex);
                                }
                            }
                        }
                    }
                    // upload file via URL
                    if (command[0].equalsIgnoreCase("/url")) {
                        URL url;
                        try {
                            url = new URL(command[1]);
                        } catch (MalformedURLException e) {
                            return "WARNING: Invalid format. Enter a URL";
                        }
                        MainServer.getServers().get(serverUnicode).getTextChannels().get(textChannelIndex).getUrls().add(url);

                        message = command[1];
                    } else if (command.length > 1 && (command[0].equalsIgnoreCase("/pin") ||
                            command[0].equalsIgnoreCase("/like") || command[0].equalsIgnoreCase("/dislike") ||
                            command[0].equalsIgnoreCase("/laugh") || command[0].equalsIgnoreCase("#pin") ||
                            command[0].equalsIgnoreCase("#likes") || command[0].equalsIgnoreCase("#dislikes") ||
                            command[0].equalsIgnoreCase("#laughs") || command[0].equalsIgnoreCase("#reactions"))) {
                        int indexOfMessage = checkIndex(MainServer.getServers().get(serverUnicode).getTextChannels().get(textChannelIndex), command[1]);
                        if (indexOfMessage == -1) {
                            return "WARNING: Invalid format";
                        } else if (indexOfMessage == -2) {
                            return "WARNING: invalid number of message. out of boundary";
                        } else {
                            switch (command[0].toLowerCase()) {
                                case "/pin" -> {
                                    pinMessage(MainServer.getServers().get(serverUnicode).getTextChannels().get(textChannelIndex), indexOfMessage);
                                    message = "NOTIFICATION: " + sender + " pinned \"" + MainServer.getServers().get(serverUnicode).getTextChannels().get(textChannelIndex).getMessages().get(indexOfMessage) + "\"";
                                }
                                case "/like" -> {
                                    likeMessage(MainServer.getServers().get(serverUnicode).getTextChannels().get(textChannelIndex), indexOfMessage);
                                    message = "NOTIFICATION: " + sender + " liked \"" + MainServer.getServers().get(serverUnicode).getTextChannels().get(textChannelIndex).getMessages().get(indexOfMessage) + "\"";
                                }
                                case "/dislike" -> {
                                    dislikeMessage(MainServer.getServers().get(serverUnicode).getTextChannels().get(textChannelIndex), indexOfMessage);
                                    message = "NOTIFICATION: " + sender + " disliked \"" + MainServer.getServers().get(serverUnicode).getTextChannels().get(textChannelIndex).getMessages().get(indexOfMessage) + "\"";
                                }
                                case "/laugh" -> {
                                    laughMessage(MainServer.getServers().get(serverUnicode).getTextChannels().get(textChannelIndex), indexOfMessage);
                                    message = "NOTIFICATION: " + sender + " laughed \"" + MainServer.getServers().get(serverUnicode).getTextChannels().get(textChannelIndex).getMessages().get(indexOfMessage) + "\"";
                                }

                                case "#likes" -> {
                                    return MainServer.getServers().get(serverUnicode).getTextChannels().get(textChannelIndex).getTextChannelMessages().get(indexOfMessage).showLikes();
                                }
                                case "#dislikes" -> {
                                    return MainServer.getServers().get(serverUnicode).getTextChannels().get(textChannelIndex).getTextChannelMessages().get(indexOfMessage).showDislikes();
                                }
                                case "#laughs" -> {
                                    return MainServer.getServers().get(serverUnicode).getTextChannels().get(textChannelIndex).getTextChannelMessages().get(indexOfMessage).showLaugh();
                                }
                                case "#reactions" -> {
                                    return MainServer.getServers().get(serverUnicode).getTextChannels().get(textChannelIndex).getTextChannelMessages().get(indexOfMessage).showAllReactions();
                                }
                            }
                        }
                    }
                }
            }
            TextChannelMessage textChannelMessage = new TextChannelMessage(message);
            // updating database and server
            synchronized (MainServer.getServers().get(serverUnicode).getTextChannels().get(textChannelIndex)) {
                if (!message.startsWith("NOTIFICATION: ")) {
                    textChannelMessage.setMessage((MainServer.getServers().get(serverUnicode).getTextChannels().get(textChannelIndex).getMessages().size() + 1) + "- " + sender + ": " + textChannelMessage.getMessage());
                    MainServer.getServers().get(serverUnicode).getTextChannels().get(textChannelIndex).getTextChannelMessages().add(textChannelMessage);
                }

                Server server = MainServer.getServers().get(serverUnicode);
                MainServer.getServers().replace(serverUnicode, server);
                MainServer.updateDatabase(server);
            }
            TextChannel updatedTextChannelFromMainServer = MainServer.getServers().get(serverUnicode).getTextChannels().get(textChannelIndex);

//            textChannel.getMessages().add(message);
//            MainServer.getServers().get(textChannel.getServerUnicode()).getTextChannels().set(textChannel.getId(), textChannel);

            // sending message from socket if the receiver is online and in the textChannel chat
            for (ClientHandler c : clientHandlers) {
                Model userOfClientHandler = c.getUser();
                if (userOfClientHandler != null) {
                    if (receivers.contains(userOfClientHandler.getUID())) {
                        userOfClientHandler = MainServer.getUsers().get(userOfClientHandler.getUID()); // updating userOfClientHandler
                        if (updatedTextChannelFromMainServer.getMembers().get(userOfClientHandler.getUID())) {
                            // synchronize!!!!!!!
                            synchronized (c.getMySocket()) {
                                c.getMySocket().write(textChannelMessage); // we can also write "this" object
                            }
                        }
                    }
                }
            }
            return null;  //receiver is not currently in the chat
        } else {
            return senderUser;
        }
    }

    private int checkIndex(TextChannel textChannel, String message) {
        try {
            int index = Integer.parseInt(message) - 1;
            if (index >= 0 && index < textChannel.getMessages().size()) {
                return index;
            } else {
                return -2;  // out of boundary
            }
        } catch (NumberFormatException e) {
            return -1;  // wrong format
        }
    }

    // overloaded
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
                /url <URL_Link>  -> to send link of a file so others can download it
                /file <file_address>  -> to upload a file from your pc
                #urls  -> prints list of all URLs in the chat
                #files  -> prints list of all files in the chat
                #url <index> <target_file_name>  -> download the specified url and save it with <target_file_name> name
                #file <index>  -> download the specified file and save it without changing the original name of the file
                #file <index> <target_file_name>  -> download the specified file and save it with <target_file_name> name
                /pin <index_of_message>  -> to pin message with number <index_of_message>
                /like <index_of_message>  -> to like message with number <index_of_message>
                /dislike <index_of_message>  -> to dislike message with number <index_of_message>
                /laugh <index_of_message>  -> to laugh at message with number <index_of_message>
                #pin  -> to show pinned message of the textChannel
                #likes <index_of_message>  -> to show likes to message with number <index_of_message>
                #dislikes <index_of_message>  -> to show dislikes to message with number <index_of_message>
                #laughs <index_of_message>  -> to show laughs at message with number <index_of_message>
                #reactions <index_of_message>  -> to show all reactions to message with number <index_of_message>
                """;
    }

    public String showPinnedMessage(TextChannel textChannel) {
        return textChannel.getPinnedMessage();
    }

    private void pinMessage(TextChannel textChannel, int index) {
        textChannel.setPinnedMessage(textChannel.getMessages().get(index));
    }

    private void likeMessage(TextChannel textChannel, int index) {
        textChannel.getTextChannelMessages().get(index).like(sender);
    }

    private void dislikeMessage(TextChannel textChannel, int index) {
        textChannel.getTextChannelMessages().get(index).dislike(sender);
    }

    private void laughMessage(TextChannel textChannel, int index) {
        textChannel.getTextChannelMessages().get(index).laugh(sender);
    }
}