package discord.client;

import discord.signals.LoginAction;
import discord.signals.SignUpOrChangeInfoAction;
import discord.signals.UpdateUserOnMainServerAction;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import com.gluonhq.charm.glisten.control.Avatar;

import java.io.IOException;

public class Controller {

    // backend fields:
    private Model user;
    private final MySocket mySocket;

    // the constructor:
    public Controller(Model user, MySocket mySocket) {
        this.user = user;
        this.mySocket = mySocket;
    }

    // getters:
    public Model getUser() {
        return user;
    }

    public MySocket getMySocket() {
        return mySocket;
    }

    //////////////////////////////////////////////////////////// login scene ->
    // login fields:
    @FXML
    private TextField usernameOnLoginMenu;
    @FXML
    private TextField passwordOnLoginMenu;
    @FXML
    private Label loginErrorMessage;

    // login methods:
    @FXML
    void login(Event event) throws IOException, ClassNotFoundException {
        loginErrorMessage.setText("");
        String usernameField = usernameOnLoginMenu.getText();
        String passwordField = passwordOnLoginMenu.getText();
        if (!"".equals(usernameField.trim()) && !"".equals(passwordField.trim())) {
            Model user = mySocket.sendSignalAndGetResponse(new LoginAction(usernameField, passwordField));
            if (user == null) {
                loginErrorMessage.setText("A username by this password could not be found!");
            } else {
                this.user = user;
                loadProfilePage(event);
            }
        } else {
            loginErrorMessage.setText("You have empty fields!");
        }
    }

    private void loadProfilePage(Event event) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("profilePage.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        App.loadNewScene(loader, stage, this);
        //user.setAvatarImage();
        //avatar.setImage(user.getAvatarImage());
        profileUsername.setText(user.getUsername());
        profileEmail.setText(user.getEmail());
        if (user.getPhoneNumber() != null) {
            profilePhoneNumber.setText(user.getPhoneNumber());
        } else {
            profilePhoneNumber.setText("You haven't added a phone number yet.");
        }
    }

    @FXML
    void loadSignupMenu(Event event) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("signupMenu.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        App.loadNewScene(loader, stage, this);
    }

    //////////////////////////////////////////////////////////// signup scene ->
    // signup fields
    @FXML
    private TextField email;
    @FXML
    private TextField usernameOnSignupMenu;
    @FXML
    private TextField passwordOnSignupMenu;
    @FXML
    private Label signupErrorMessage;
    @FXML
    Label conditionMessage1;
    @FXML
    Label conditionMessage2;


    // signup methods
    @FXML
    void signup(Event event) throws IOException, ClassNotFoundException {
        signupErrorMessage.setText("");
        conditionMessage1.setText("");
        conditionMessage2.setText("");
        String emailField = email.getText().trim();
        String usernameField = usernameOnSignupMenu.getText().trim();
        String passwordField = passwordOnSignupMenu.getText().trim();
        if (!"".equals(emailField) && !"".equals(usernameField) && !"".equals(passwordField)) {

            SignUpOrChangeInfoAction signupAction = new SignUpOrChangeInfoAction();

            // validating username
            signupAction.setUsername(usernameOnSignupMenu.getText());
            Boolean success = mySocket.sendSignalAndGetResponse(signupAction);
            if (success == null) {
                signupErrorMessage.setText("This username is already taken!");
                return;
            }
            if (!success) {
                signupErrorMessage.setText("Invalid username format!");
                conditionMessage1.setText("A username should consist of only English letters/numbers and be of a minimal length of 6");
                return;
            }

            // validating password
            signupAction.setPassword(passwordField);
            success = mySocket.sendSignalAndGetResponse(signupAction);
            if (!success) {
                signupErrorMessage.setText("Invalid password format!");
                conditionMessage1.setText("A password should consist of only English letters/numbers and be of a minimal length of 8");
                conditionMessage2.setText("It should also at least have 1 small and 1 capital letter and 1 number");
                return;
            }

            // validating email
            signupAction.setEmail(emailField);
            success = mySocket.sendSignalAndGetResponse(signupAction);
            if (!success) {
                signupErrorMessage.setText("Invalid email format!");
                return;
            }

            // no phone number is taken from the user at first
            signupAction.setPhoneNumber("");
            mySocket.sendSignalAndGetResponse(signupAction); // always returns true and gets ignored

            signupAction.finalizeStage();
            user = mySocket.sendSignalAndGetResponse(signupAction);  // we can get the signed-up user here but ignore for now
            //loadLoginMenu(event);
            loadProfilePage(event);
        }
    }

    @FXML
    void loadLoginMenu(Event event) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("loginMenu.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        App.loadNewScene(loader, stage, this);
    }

    //////////////////////////////////////////////////////////// profile page scene ->
    // profile fields:
    @FXML
    private Avatar avatar;
    @FXML
    private Label status;
    @FXML
    private TextField profileUsername;
    @FXML
    private TextField profileEmail;
    @FXML
    private TextField profilePhoneNumber;
    @FXML
    private Button editButton;
    @FXML
    private Label editErrorMessage;

    @FXML
    void editEnabled() throws IOException, ClassNotFoundException {
        switch (editButton.getText()) {
            case "Edit" -> {
                profileUsername.setEditable(true);
                profileEmail.setEditable(true);
                profilePhoneNumber.setEditable(true);
                editButton.setText("Done");
            }
            case "Done" -> {

                SignUpOrChangeInfoAction changeInfoAction = new SignUpOrChangeInfoAction(user.getUsername());

                changeInfoAction.setUsername(profileUsername.getText());
                boolean validUsername = mySocket.sendSignalAndGetResponse(changeInfoAction);

                changeInfoAction.setEmail(profileEmail.getText());
                boolean validEmail = mySocket.sendSignalAndGetResponse(changeInfoAction);

                changeInfoAction.setUsername(profilePhoneNumber.getText());
                boolean validPhoneNumber = (boolean) mySocket.sendSignalAndGetResponse(changeInfoAction) || "".equals(profilePhoneNumber.getText());

                if (!validUsername) {
                    editErrorMessage.setText("Invalid username!");
                } else if (!validEmail) {
                    editErrorMessage.setText("Invalid email");
                } else if (!validPhoneNumber) {
                    editErrorMessage.setText("Invalid phone number (you can empty this field to remove your phone number");
                } else {
                    profileUsername.setEditable(false);
                    profileEmail.setEditable(false);
                    profilePhoneNumber.setEditable(false);
                    editButton.setText("Edit");

                    String oldUsername = user.getUsername();
                    user.setUsername(profileUsername.getText());
                    user.setEmail(profileEmail.getText());
                    user.setPhoneNumber(profilePhoneNumber.getText());
                    if ("".equals(user.getPhoneNumber().trim())) {
                        profilePhoneNumber.setText("You haven't added a phone number yet.");
                    }
                    boolean DBConnect = mySocket.sendSignalAndGetResponse(new UpdateUserOnMainServerAction(user, oldUsername));
                }
            }
        }
    }
}