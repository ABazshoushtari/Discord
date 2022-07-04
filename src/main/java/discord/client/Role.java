package discord.client;

import java.io.Serializable;
import java.util.HashSet;

public class Role implements Serializable {
    // Fields:
    private String roleName;
    private final HashSet<Ability> abilities;

    // Constructors:
    public Role(String roleName, HashSet<Ability> abilities) {
        this.roleName = roleName;
        this.abilities = abilities;
    }

    // Getters:
    public HashSet<Ability> getAbilities() {
        return abilities;
    }

    public String getRoleName() {
        return roleName;
    }

    // Setters:
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    //Other Methods:
    @Override
    public String toString() {
        return "Role{" +
                "roleName='" + roleName + '\'' +
                ", abilities=" + abilities +
                '}';
    }
}
