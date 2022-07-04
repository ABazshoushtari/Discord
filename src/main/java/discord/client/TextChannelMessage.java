package discord.client;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

public class TextChannelMessage implements Serializable {
    // Fields:
    private String message;
    private HashMap<String, HashSet<Reaction>> reactions;

    // Constructors:
    public TextChannelMessage(String message) {
        this.message = message;
        reactions = new HashMap<>();
    }

    // Methods:
    // Getter Methods:
    public String getMessage() {
        return message;
    }

    public HashMap<String, HashSet<Reaction>> getReactions() {
        return reactions;
    }

    // Setter Methods:
    public void setMessage(String message) {
        this.message = message;
    }

    public void setReactions(HashMap<String, HashSet<Reaction>> reactions) {
        this.reactions = reactions;
    }

    // Other Methods:
    public void like(String username) {
        if (!reactions.containsKey(username)) {
            reactions.put(username, new HashSet<>());
        }
        reactions.get(username).add(Reaction.LIKE);
    }

    public void dislike(String username) {
        if (!reactions.containsKey(username)) {
            reactions.put(username, new HashSet<>());
        }
        reactions.get(username).add(Reaction.DISLIKE);
    }

    public void laugh(String username) {
        if (!reactions.containsKey(username)) {
            reactions.put(username, new HashSet<>());
        }
        reactions.get(username).add(Reaction.LAUGH);
    }

    public String showLikes() {
        String output = "\"" + message + "\"" + " liked by: ";
        for (String s : reactions.keySet()) {
            if (reactions.get(s).contains(Reaction.LIKE)) {
                output = output.concat(s + ", ");
            }
        }
        return output.endsWith(", ") ? output.substring(0, output.length() - 2) : output;
    }

    public String showDislikes() {
        String output = "\"" + message + "\"" + " disliked by: ";
        for (String s : reactions.keySet()) {
            if (reactions.get(s).contains(Reaction.DISLIKE)) {
                output = output.concat(s + ", ");
            }
        }
        return output.endsWith(", ") ? output.substring(0, output.length() - 2) : output;
    }

    public String showLaugh() {
        String output = "\"" + message + "\"" + " laughed by: ";
        for (String s : reactions.keySet()) {
            if (reactions.get(s).contains(Reaction.LAUGH)) {
                output = output.concat(s + ", ");
            }
        }
        return output.endsWith(", ") ? output.substring(0, output.length() - 2) : output;
    }

    public String showAllReactions() {
        return "reactions to \"" + message + "\"\n" + showLikes().split("\"")[2] + "\n" + showDislikes().split("\"")[2] + "\n" + showLaugh().split("\"")[2];
    }
}
