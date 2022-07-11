package discord.client;

import discord.signals.*;

import java.io.IOException;
import java.util.*;

public class Server implements Asset {
    // Fields:
    private final int unicode;
    private String serverName;
    private final Integer creatorUID;
    private byte[] avatarImage;
    private String avatarContentType;  // type of avatarImage
    private HashMap<String, Role> serverRoles;      // maps the roles' names to their Role object
    private HashMap<Integer, HashSet<Role>> members;     // maps the members' IDs to their set of roles
    private ArrayList<TextChannel> textChannels;
    private HashSet<Integer> bannedUsers;

    // Constructors:
    public Server(int unicode, String serverName, int creatorUID) {
        // construct and initialize the fields
        this.unicode = unicode;
        this.serverName = serverName;
        this.creatorUID = creatorUID;
        serverRoles = new HashMap<>();
        members = new HashMap<>();
        textChannels = new ArrayList<>();
        bannedUsers = new HashSet<>();

        //a "member" role with just the SeeChatHistory Ability is added to the roles of the server
        Role memberRole = new Role("member", new HashSet<>(List.of(Ability.SeeChatHistory, Ability.PinMessage)));
        serverRoles.put(memberRole.getRoleName(), memberRole);

        //give the owner an "ownerRole" (containing all the abilities), as well as the member role
        HashSet<Ability> ownerAbilities = new HashSet<>(Arrays.asList(Ability.values()));
        Role ownerRole = new Role("owner", ownerAbilities);
        HashSet<Role> ownerRoleSet = new HashSet<>(List.of(ownerRole, memberRole));
        members.put(creatorUID, ownerRoleSet);

        //initialize the first default text channel called general with just a creator member
        textChannels.add(new TextChannel("general", new HashSet<>(List.of(creatorUID))));
    }

    // Getters:
    public int getUnicode() {
        return unicode;
    }
    public Integer getID() {
        return unicode;
    }

    public Integer getCreatorUID() {
        return creatorUID;
    }

    public String getServerName() {
        return serverName;
    }

    public byte[] getAvatarImage() {
        return avatarImage;
    }

    public String getAvatarContentType() {
        return avatarContentType;
    }

    public HashMap<Integer, HashSet<Role>> getMembers() {
        return members;
    }

    public ArrayList<TextChannel> getTextChannels() {
        return textChannels;
    }


    // Setters:
    public void setAvatarImage(byte[] avatarImage) {
        this.avatarImage = avatarImage;
    }

    public void setAvatarContentType(String avatarContentType) {
        this.avatarContentType = avatarContentType;
    }

    // Other Methods:
    public void enter(Controller clientController) throws IOException, ClassNotFoundException {
        outer:
        while (true) {

            //clientController.getPrinter().printServerMenu();
            //int command = clientController.getMyScanner().getInt(1, 6);

            //update server from MainServer
            selfUpdate(clientController);

            // get all the member's abilities
            String myUsername = clientController.getUser().getUsername();

//            switch (command) {
//                case 1 -> changeInfo(clientController, myUsername);
//                case 2 -> changeMembers(clientController, myUsername);
//                case 3 -> changeTextChannels(clientController, myUsername);
//                case 4 -> enterATextChannel(clientController, myUsername);
//                case 5 -> seeAllMembersRoles();
//                case 6 -> {
//                    break outer;
//                }
//            }
        }
    }

    private void selfUpdate(Controller clientController) throws IOException, ClassNotFoundException {
        Server updatedThis = clientController.getMySocket().sendSignalAndGetResponse(new GetServerFromMainServerAction(unicode));
        if (updatedThis == null) {
            //clientController.getPrinter().printErrorMessage("db");
            return;
        }
        updateThisFromMainServer(updatedThis);
    }

    public boolean updateThisOnMainServer(Controller clientController) throws IOException, ClassNotFoundException {
        return clientController.getMySocket().sendSignalAndGetResponse(new UpdateServerOnMainServerAction(this));
    }

    private void updateThisFromMainServer(Server updatedThis) {
        serverName = updatedThis.serverName;
        serverRoles = updatedThis.serverRoles;
        members = updatedThis.members;
        textChannels = updatedThis.textChannels;
        bannedUsers = updatedThis.bannedUsers;
    }

    private void seeAllMembersRoles() {
        for (int ID : members.keySet()) {
            HashSet<Role> roles = members.get(ID);
            System.out.println(ID);
            for (Role role : roles) {
                System.out.println(role.toString());
            }
        }
    }

    private void changeInfo(Controller clientController, String myUsername) throws IOException, ClassNotFoundException {
        outer:
        while (true) {
            //clientController.getPrinter().printServerChangeInfoMenu();
            // int command = clientController.getMyScanner().getInt(1, 4);
//            switch (command) {
//                case 1 -> {
//                    if (getAllAbilities(myUsername).contains(Ability.ChangeServerName)) {
//                        clientController.getPrinter().printGetMessage("new server name");
//                        clientController.getPrinter().printGoBackMessage();
//                        serverName = clientController.getMyScanner().getLine();
//                        if ("".equals(serverName)) {
//                            break;
//                        }
//                        clientController.getPrinter().printSuccessMessage("server name change");
//                    } else {
//                        clientController.getPrinter().printErrorMessage("server name change");
//                    }
//                }
//                case 2 -> {
//                    if (getAllAbilities(myUsername).contains(Ability.Owner)) {
//                        createOrEditARole(clientController);
//                    } else {
//                        clientController.getPrinter().printErrorMessage("Only the owner can access this part!");
//                    }
//                }
//                case 3 -> {
//                    break outer;
//                }
//            }
//            if (!updateThisOnMainServer(clientController)) {
//                clientController.getPrinter().printErrorMessage("db");
//            }
        }
    }

    private void createOrEditARole(Controller clientController) {
//        clientController.getPrinter().printRoleEditMenu();
//        switch (clientController.getMyScanner().getInt(1, 3)) {
//            case 1 -> {
//                Role newRole = createNewRole(clientController);
//                clientController.getPrinter().println("Enter the usernames of the members you want to give this role to");
//                clientController.getPrinter().println("the usernames must be seperated by a space (invalid usernames will be ignored)");
//                clientController.getPrinter().printSetList(members.keySet());
//                clientController.getPrinter().printGoBackMessage();
//                String list = clientController.getMyScanner().getLine();
//                if ("".equals(list)) {
//                    break;
//                }
//                addInitialRoleHolders(newRole, list);
//            }
//            case 2 -> editARole(clientController);
//        }
    }

    private Role createNewRole(Controller clientController) {
//        clientController.getPrinter().println("What is the name of the new role?");
//        String newRoleName;
//        do {
//            newRoleName = clientController.getMyScanner().getLine();
//        } while ("".equals(newRoleName.trim()));
//        clientController.getPrinter().println("What abilities does this role have?");
//        clientController.getPrinter().printAbilityList();
//        clientController.getPrinter().println("(enter the numbers seperated by a space)");
//        ArrayList<Integer> abilityIndexes = clientController.getMyScanner().getIntList(8); // indexes received range: 0-7
//        Role newRole = new Role(newRoleName, new HashSet<>());
//        for (int abilityIndex : abilityIndexes) {
//            newRole.getAbilities().add(Ability.values()[abilityIndex + 1]); // ability 0 is only for the owner
//        }
//        serverRoles.put(newRole.getRoleName(), newRole);
//        clientController.getPrinter().printSuccessMessage("new role");
//        return newRole;
        return null;
    }

    private void addInitialRoleHolders(Role newRole, ArrayList<Integer> list) {
        for (int UID : list) {
            if (members.containsKey(UID)) {
                members.get(UID).add(newRole);
            }
        }
    }

    private void editARole(Controller clientController) {
//        clientController.getPrinter().printGetMessage("enter the name of the role you want to edit");
//        clientController.getPrinter().printSetList(serverRoles.keySet());
//        clientController.getPrinter().printGoBackMessage();
//        String roleName;
//        while (true) {
//            roleName = clientController.getMyScanner().getLine();
//            if ("".equals(roleName)) {
//                return;
//            }
//            if (serverRoles.containsKey(roleName)) {
//                break;
//            } else {
//                clientController.getPrinter().printErrorMessage("Invalid name!");
//            }
//        }
//        clientController.getPrinter().println("What abilities will this role have after the change?");
//        clientController.getPrinter().printAbilityList();
//        clientController.getPrinter().println("(enter the numbers seperated by a space)");
//        ArrayList<Integer> abilityIndexes = clientController.getMyScanner().getIntList(8); // indexes received range: 0-7
//        Role roleUnderEdit = serverRoles.get(roleName);
//        roleUnderEdit.getAbilities().clear();
//        for (int abilityIndex : abilityIndexes) {
//            roleUnderEdit.getAbilities().add(Ability.values()[abilityIndex + 1]); // ability 0 is only for the owner
//        }
//        clientController.getPrinter().printSuccessMessage("edit role");
    }

    private void changeMembers(Controller clientController, String myUsername) throws IOException, ClassNotFoundException {
//        clientController.getPrinter().printMembersEditMenu();
//        int command = clientController.getMyScanner().getInt(1, 4);
//        switch (command) {
//            case 1 -> {
//                if (addFriendsToServer(clientController)) {
//                    clientController.getPrinter().printSuccessMessage("friend add");
//                }
//            }
//            case 2 -> {
//                if (getAllAbilities(myUsername).contains(Ability.RemoveMember)) {
//                    Boolean keepGoing;
//                    do {
//                        keepGoing = removeOrBanMembersFromServer(clientController, false);
//                        if (keepGoing == null) break;
//                    } while (keepGoing);
//                } else {
//                    clientController.getPrinter().printErrorMessage("permission");
//                }
//            }
//            case 3 -> {
//                if (getAllAbilities(myUsername).contains(Ability.Ban)) {
//                    Boolean keepGoing;
//                    do {
//                        keepGoing = removeOrBanMembersFromServer(clientController, true);
//                        if (keepGoing == null) break;
//                    } while (keepGoing);
//                } else {
//                    clientController.getPrinter().printErrorMessage("permission");
//                }
//            }
//        }
    }

    public boolean addFriendsToServer(Controller clientController) throws IOException, ClassNotFoundException {

//        Model user = clientController.getUser();
//        View printer = clientController.getPrinter();
//        MyScanner myScanner = clientController.getMyScanner();
//        MySocket mySocket = clientController.getMySocket();
//
//        printer.println("Who do you want to add to the server?");
//        printer.println("enter the indexes seperated by a space!");
//
//        // print the list of the friends that are not a member of the server
//        LinkedList<String> notAddedFriends = user.getFriends();
//        selfUpdate(clientController);
//        for (String member : members.keySet()) {
//            notAddedFriends.remove(member);
//        }
//        printer.printList(notAddedFriends);
//        printer.println("enter 0 to select no one");
//
//        // get the indexes of the not added friends you want to add and add them to the server
//        ArrayList<String> addedFriends = new ArrayList<>();
//        for (int friendIndex : myScanner.getIntList(notAddedFriends.size())) {
//            String friendUsername = notAddedFriends.get(friendIndex);
//            if (addNewMember(friendUsername)) {
//                clientController.getPrinter().printSuccessMessage("add friend");
//                clientController.getPrinter().println(friendUsername);
//            } else {
//                clientController.getPrinter().printErrorMessage("ban");
//                clientController.getPrinter().println(friendUsername);
//            }
//            addedFriends.add(friendUsername);
//        }
//
//        // update this server on the MainServer
//        boolean DBConnect = mySocket.sendSignalAndGetResponse(new UpdateServerOnMainServerAction(this));
//        if (!DBConnect) {
//            printer.printErrorMessage("db");
//            return false;
//        }
//        // send the signal to the added friends that they're added and update this on MainServer
//        for (String addedFriend : addedFriends) {
//            DBConnect = mySocket.sendSignalAndGetResponse(new AddFriendToServerAction(unicode, addedFriend));
//            if (!DBConnect) {
//                printer.printErrorMessage("db");
//                return false;
//            }
//        }
        return true;
    }

    public boolean addNewMember(int UID) {
        if (!bannedUsers.contains(UID)) {
            members.put(UID, new HashSet<>(List.of(serverRoles.get("member"))));  // anyone gets the "member" role
            //anyone gets added all the text channels
            for (TextChannel textChannel : textChannels) {
                textChannel.getMembers().put(UID, false);
            }
            return true;
        }
        return false;
    }

    public void addNewTextChannel(String newTextChannelName) {
        textChannels.add(new TextChannel(newTextChannelName, members.keySet()));
    }

    private Boolean removeOrBanMembersFromServer(Controller clientController, boolean ban) throws IOException, ClassNotFoundException {

//        View printer = clientController.getPrinter();
//        MyScanner myScanner = clientController.getMyScanner();
//        MySocket mySocket = clientController.getMySocket();
//
//        printer.println("Who do you want to remove/ban from the server?");
//        printer.println("Enter their name (invalid username will be ignored!)");
//        printer.printGoBackMessage();
//
//        // print the list of the members of the server and get the member's username whom will be removed
//        selfUpdate(clientController);
//        printer.printSetList(members.keySet());
//        String beingRemovedOrBannedMember = myScanner.getLine();
//        if ("".equals(beingRemovedOrBannedMember)) {
//            return false;
//        }
//        members.remove(beingRemovedOrBannedMember);
//
//        // also remove them from all the text channels
//        for (TextChannel textChannel : textChannels) {
//            textChannel.removeMember(beingRemovedOrBannedMember);
//        }
//
//        // also add to the banned list of this is a ban action
//        if (ban) {
//            bannedUsers.add(beingRemovedOrBannedMember);
//        }
//
//        // update this server on the MainServer
//        boolean DBConnect = mySocket.sendSignalAndGetResponse(new UpdateServerOnMainServerAction(this));
//        if (!DBConnect) {
//            printer.printErrorMessage("db");
//            return null;
//        }
//
//        // send the signal to the removed member that they're removed and update this on MainServer
//        DBConnect = mySocket.sendSignalAndGetResponse(new removeFriendFromServerAction(unicode, beingRemovedOrBannedMember));
//        return clientController.keepGoing(DBConnect);
        return null;
    }

    private void changeTextChannels(Controller clientController, String myUsername) throws IOException, ClassNotFoundException {
//        clientController.getPrinter().printTextChannelsEditMenu();
//        int command = clientController.getMyScanner().getInt(1, 5);
//        selfUpdate(clientController);
//        switch (command) {
//            case 1 -> {
//                if (getAllAbilities(myUsername).contains(Ability.CreateChannel)) {
//                    if (createNewTextChannel(clientController)) {
//                        clientController.getPrinter().printSuccessMessage("channel add");
//                    } else {
//                        clientController.getPrinter().printErrorMessage("db");
//                    }
//                } else {
//                    clientController.getPrinter().printErrorMessage("permission");
//                }
//            }
//            case 2 -> {
//                if (getAllAbilities(myUsername).contains(Ability.RemoveChannel)) {
//                    if (removeTextChannel(clientController)) {
//                        clientController.getPrinter().printSuccessMessage("channel remove");
//                    } else {
//                        clientController.getPrinter().printErrorMessage("db");
//                    }
//                } else {
//                    clientController.getPrinter().printErrorMessage("permission");
//                }
//            }
//            case 3 -> {
//                if (getAllAbilities(myUsername).contains(Ability.CreateChannel) || getAllAbilities(myUsername).contains(Ability.RemoveChannel)) {
//                    clientController.getPrinter().printTextChannelList(textChannels);
//                    clientController.getPrinter().println("enter 0 to go back");
//                    int index = clientController.getMyScanner().getInt(0, textChannels.size());
//                    if (index == 0) {
//                        break;
//                    }
//                    clientController.getPrinter().printGetMessage("new text channel name");
//                    String newTextChannelName = clientController.getMyScanner().getLine();
//                    textChannels.get(index - 1).setName(newTextChannelName);
//                } else {
//                    clientController.getPrinter().printErrorMessage("permission");
//                }
//            }
//            case 4 -> {
//                if (getAllAbilities(myUsername).contains(Ability.LimitMembersOfChannels)) {
//                    Boolean keepGoing;
//                    do {
//                        keepGoing = limitMemberFromATextChannel(clientController);
//                        if (keepGoing == null) break;
//                    } while (keepGoing);
//                } else {
//                    clientController.getPrinter().printErrorMessage("permission");
//                }
//            }
//        }
    }

    private boolean createNewTextChannel(Controller clientController) throws IOException, ClassNotFoundException {
//        clientController.getPrinter().println("Enter the name of the new channel");
//        String newTextChannelName = clientController.getMyScanner().getLine();
//        textChannels.add(new TextChannel(newTextChannelName, members.keySet()));
//        return updateThisOnMainServer(clientController);
        return false;
    }

    private boolean removeTextChannel(Controller clientController) throws IOException, ClassNotFoundException {
//        clientController.getPrinter().println("Enter the index of the channel you want to remove");
//        int index = clientController.getMyScanner().getInt(1, textChannels.size());
//        textChannels.remove(index);
//        return updateThisOnMainServer(clientController);
        return false;
    }

    private void enterATextChannel(Controller clientController, String myUsername) throws IOException, ClassNotFoundException {

//        Model user = clientController.getUser();
//        View printer = clientController.getPrinter();
//        MyScanner myScanner = clientController.getMyScanner();
//        MySocket mySocket = clientController.getMySocket();
//
//        selfUpdate(clientController);
//        ArrayList<TextChannel> myTextChannels = printer.printTextChannelListForMembers(textChannels, myUsername);
//        if (myTextChannels.size() == 0) {
//            printer.println("You're not a part of any text channel on this server!");
//            return;
//        }
//        printer.printGoBackMessage(0);
//        int index = clientController.getMyScanner().getInt(0, myTextChannels.size()) - 1;
//        if (index == -1) {
//            return;
//        }
//        TextChannel selectedTextChannel = myTextChannels.get(index);
//        index = textChannels.indexOf(selectedTextChannel);
//
//        selectedTextChannel.getMembers().replace(myUsername, true);
//        if (!updateThisOnMainServer(clientController)) {
//            printer.printErrorMessage("db");
//        }
//
//        ArrayList<String> receivers = new ArrayList<>(selectedTextChannel.getMembers().keySet());
//        receivers.remove(myUsername);   // remove oneself from the receivers
//
//        // printing previous messages for the people who have the access to see chat history
//        if (getAllAbilities(myUsername).contains(Ability.SeeChatHistory)) {
//            printer.printList(selectedTextChannel.getMessages());
//        }
//
//        // receiving messages
//        Thread listener = new Thread(new TextChannelListener(clientController));
//        listener.start();
//
//        // sending message
//        printer.println("enter \"#exit\" to exit the chat");
//        printer.println("enter \"#help\" to see the commands guide");
//        while (true) {
//            String message = myScanner.getLine();
//            try {
//                if (message.toLowerCase().startsWith("/pin ") && !getAllAbilities(myUsername).contains(Ability.PinMessage)) {
//                    printer.printErrorMessage("permission");
//                    continue;
//                }
//                mySocket.write(new TextChannelChatAction(myUsername, message, unicode, index, receivers));
//                if (message.equals("#exit")) {
//                    break;
//                }
//            } catch (IOException e) {
//                //textChannels.get(index).getMembers().replace(myUsername, false);
//                printer.printErrorMessage("IO");
//            }
//        }
//
//        synchronized (user.getUsername()) {
//            try {
//                user.getUsername().wait();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            selectedTextChannel = mySocket.sendSignalAndGetResponse(new UpdateTextChannelOfServerFromMainServer(unicode, index));
//            selectedTextChannel.getMembers().replace(myUsername, false);
//        }
//        boolean DBConnect = mySocket.sendSignalAndGetResponse(new UpdateTextChannelOfServerOnMainServer(unicode, index, selectedTextChannel));
//        if (!DBConnect) {
//            printer.printErrorMessage("db");
//        }
    }

    private Boolean limitMemberFromATextChannel(Controller clientController) throws IOException, ClassNotFoundException {

//        View printer = clientController.getPrinter();
//        MyScanner myScanner = clientController.getMyScanner();
//
//        printer.println("Which text channel do you want to limit/give back the access to a member from?");
//        printer.printTextChannelList(textChannels);
//        int index = myScanner.getInt(1, textChannels.size()) - 1;
//        printer.println("Enter the name of the member you want to limit/give access back from this text channel (invalid name will be ignored)");
//        selfUpdate(clientController);
//        printer.printSetList(textChannels.get(index).getMembers().keySet());
//        printer.printGoBackMessage();
//        String member = myScanner.getLine();
//        if ("".equals(member)) {
//            return false;
//        }
//        printer.println("1. Limit");
//        printer.println("2. Give access back");
//        switch (myScanner.getInt(1, 2)) {
//            case 1 -> textChannels.get(index).getMembers().remove(member);
//            case 2 -> textChannels.get(index).getMembers().put(member, false);
//        }
//
//        // update this server on the MainServer
//        boolean DBConnect = clientController.getMySocket().sendSignalAndGetResponse(new UpdateServerOnMainServerAction(this));
//        return clientController.keepGoing(DBConnect);
        return false;
    }

    private HashSet<Ability> getAllAbilities(int UID) {
        HashSet<Ability> abilities = new HashSet<>();
        for (Role role : members.get(UID)) {
            abilities.addAll(role.getAbilities());
        }
        return abilities;
    }
}
