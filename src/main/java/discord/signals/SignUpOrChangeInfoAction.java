package discord.signals;

import discord.mainServer.MainServer;
import discord.client.Model;

import java.util.regex.Pattern;

public class SignUpOrChangeInfoAction implements Action {
    // Fields:
    private int UID; // used when changing info
    private String username;
    private String oldUsername; // used when changing username
    private String password;
    private String email;
    private String phoneNumber;
    private int stage;
    // when signing up always constructed 0
    // 1-4 for getting newUser fields when signing up, 5 for finalizing the signUp
    // -1 for changing one of the fields
    private int subStage;   // only actually used for stage == 6 (subStage always equals stage)
    private String regex;

    // Constructors:
    public SignUpOrChangeInfoAction() {
        stage = 0;
    }       // used when signing up

    public SignUpOrChangeInfoAction(String username) {      //used when changing a field from the user
        UID = MainServer.getIDs().get(username);
        this.username = username;
        this.stage = -1;
    }

    // Getters:
    public int getStage() {
        return stage;
    }

    // Other Methods:
    public void finalizeStage() {
        stage = 5;
    }

    public void setUsername(String newUsername) {
        if (stage != -1) {
            this.username = newUsername;
            stage = 1;
        } else {
            oldUsername = this.username;
            this.username = newUsername;
            subStage = 1;
        }
        regex = "^\\w{6,}$";
    }

    public void setPassword(String password) {
        this.password = password;
        if (stage != -1) {
            stage = 2;
        } else {
            subStage = 2;
        }
        regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d+]{8,}$";
    }

    public void setEmail(String email) {
        this.email = email;
        if (stage != -1) {
            stage = 3;
        } else {
            subStage = 3;
        }
        regex = "^\\w+$";
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        if (stage != -1) {
            stage = 4;
        } else {
            subStage = 4;
        }
        regex = "^\\d{11}$";
    }

    @Override
    public Object act() {
        switch (stage) {
            // case 1-5: signing up processes
            case 1 -> {
                if (MainServer.getIDs().containsKey(username)) {
                    return null;
                }
                return isMatched(username);
            }
            case 2 -> {
                return isMatched(password);
            }
            case 3 -> {
                return isAValidEmail(email);
            }
            case 4 -> {
                if ("0".equals(phoneNumber)) {
                    return true;
                } else {
                    return isMatched(phoneNumber);
                }
            }
            case 5 -> {
                if ("0".equals(phoneNumber)) phoneNumber = null;
                // generate a new ID for the new user
                int UID = 0;
                while (MainServer.getIDs().containsValue(UID)) {
                    UID++;
                }
                Model newUser = new Model(UID, username, password, email, phoneNumber);
                if (MainServer.signUpUser(newUser)) {
                    return newUser;
                }
            }
            // change one of the fields process:
            case -1 -> {
                boolean match = false;
                boolean DBConnect = true;
                Model changedUser = MainServer.getUsers().get(UID);
                switch (subStage) {
                    case 1 -> {
                        match = isMatched(username);
                        if (match)  changedUser.setUsername(username);
                    }
                    case 2 -> {
                        match = isMatched(password);
                        if (match) changedUser.setPassword(password);
                    }
                    case 3 -> {
                        match = isAValidEmail(email);
                        if (match) changedUser.setEmail(email);
                    }
                    case 4 -> {
                        if ("0".equals(phoneNumber)) {
                            changedUser.setPhoneNumber(null);
                            return true;
                        }
                        match = isMatched(phoneNumber);
                        if (match) changedUser.setPhoneNumber(phoneNumber);
                    }
                }
                if (match) {
                    if (subStage == 1) {    // when changing username
                        MainServer.getIDs().remove(oldUsername);
                        MainServer.getIDs().put(username, UID);
                    }
                    MainServer.getUsers().replace(UID, changedUser);
                    DBConnect = MainServer.updateDatabase(changedUser);
                }
                return match && DBConnect;
            }
        }
        return null;
    }

    private boolean isMatched(String input) {
        return Pattern.matches(regex, input);
    }

    private boolean isAValidEmail(String email) {
        String[] emailDividedByAtSign = email.split("@");
        if (emailDividedByAtSign.length != 2) {     // should have exactly one @
            return false;
        }
        if (emailDividedByAtSign[1].split("\\.").length < 2) {    // should have at least 1 dot after @
            return false;
        }
        boolean validStartAndFinish = validStartAndFinish(email);
        boolean charactersAreValid = true;
        for (String part : emailDividedByAtSign) {
            validStartAndFinish = validStartAndFinish && validStartAndFinish(part);
            for (String subPart : part.split("\\.")) {
                charactersAreValid = charactersAreValid && isMatched(subPart);
            }
        }
        return charactersAreValid && validStartAndFinish;
    }

    private boolean validStartAndFinish(String email) {
        String checkStartAndFinish = email.replace(".", " ");
        return checkStartAndFinish.length() == checkStartAndFinish.trim().length();
    }
}