package discord.client;


import java.io.IOException;
import java.util.*;

public class Server implements Asset {
    // Fields:
    private final Integer unicode;
    private String serverName;
    private final Integer creatorUID;
    private byte[] avatarImage;
    private final HashMap<String, Role> serverRoles;      // maps the roles' names to their Role object
    private final HashMap<Integer, HashSet<Role>> members;     // maps the members' IDs to their set of roles
    private final ArrayList<TextChannel> textChannels;
    private final HashSet<Integer> bannedUsers;

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
        textChannels.add(new TextChannel("general", 0, new HashSet<>(List.of(creatorUID))));
    }

    // Getters:
    public Integer getID() {
        return unicode;
    }

    public Integer getUnicode() {
        return unicode;
    }

    public String getServerName() {
        return serverName;
    }

    public Integer getCreatorUID() {
        return creatorUID;
    }

    @Override
    public byte[] getAvatarImage() {
        return avatarImage;
    }

    public HashMap<String, Role> getServerRoles() {
        return serverRoles;
    }

    public HashMap<Integer, HashSet<Role>> getMembers() {
        return members;
    }

    public ArrayList<TextChannel> getTextChannels() {
        return textChannels;
    }

    public HashSet<Integer> getBannedUsers() {
        return bannedUsers;
    }

    // Setters:
    public void setAvatarImage(byte[] avatarImage) {
        this.avatarImage = avatarImage;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    private void addInitialRoleHolders(Role newRole, ArrayList<Integer> list) {
        for (int UID : list) {
            if (members.containsKey(UID)) {
                members.get(UID).add(newRole);
            }
        }
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

    public TextChannel addNewTextChannel(String newTextChannelName) {
        TextChannel newTextChannel = new TextChannel(newTextChannelName, textChannels.size(), members.keySet());
        textChannels.add(newTextChannel);
        return newTextChannel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Server)) return false;
        Server server = (Server) o;
        return getUnicode().equals(server.getUnicode());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUnicode());
    }

    public HashSet<Ability> getAllAbilities(int UID) {
        HashSet<Ability> abilities = new HashSet<>();
        for (Role role : members.get(UID)) {
            abilities.addAll(role.getAbilities());
        }
        return abilities;
    }
}
