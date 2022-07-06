package discord.client;

import discord.signals.LoginAction;
import discord.signals.LogoutAction;
import discord.signals.SignUpOrChangeInfoAction;
import discord.signals.UpdateUserOnMainServerAction;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
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
        loadScene(event, "profilePage.fxml");
        //user.setAvatarImage();
        //avatar.setImage(user.getAvatarImage());
        profileUsername.setText(user.getUsername());
        profileEmail.setText(user.getEmail());
        setStatusLabel(user.getStatus());
        if (user.getPhoneNumber() != null) {
            profilePhoneNumber.setText(user.getPhoneNumber());
        } else {
            profilePhoneNumber.setText("You haven't added a phone number yet.");
        }
    }

    private void setStatusLabel(Status status) {
        switch (status) {
            case Online -> {
                profileStatus.setText("Online");
                profileStatus.setTextFill(new Color(0, 1, 0, 1));
            }
            case Idle -> {
                profileStatus.setText("Idle");
                profileStatus.setTextFill(new Color(1, 0.647, 0, 1));
            }
            case DoNotDisturb -> {
                profileStatus.setText("Do Not Disturb");
                profileStatus.setTextFill(new Color(1, 0, 0, 1));
            }
            case Invisible -> {
                profileStatus.setText("Invisible");
                profileStatus.setTextFill(new Color(0.502, 0.502, 0.502, 1));
            }
        }
    }

    private void loadScene(Event event, String sceneName) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(sceneName));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        try {
            loader.setController(this);
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void loadSignupMenu(Event event) {
        loadScene(event, "signupMenu.fxml");
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
            Boolean valid = mySocket.sendSignalAndGetResponse(signupAction);
            if (valid == null) {
                signupErrorMessage.setText("This username is already taken!");
                return;
            }
            if (!valid) {
                signupErrorMessage.setText("Invalid username format!");
                conditionMessage1.setText("A username should consist of only English letters/numbers and be of a minimal length of 6");
                return;
            }

            // validating password
            signupAction.setPassword(passwordField);
            valid = mySocket.sendSignalAndGetResponse(signupAction);
            if (!valid) {
                signupErrorMessage.setText("Invalid password format!");
                conditionMessage1.setText("A password should consist of only English letters/numbers and be of a minimal length of 8");
                conditionMessage2.setText("It should also at least have 1 small and 1 capital letter and 1 number");
                return;
            }

            // validating email
            signupAction.setEmail(emailField);
            valid = mySocket.sendSignalAndGetResponse(signupAction);
            if (!valid) {
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
        loadScene(event, "loginMenu.fxml");
    }

    //////////////////////////////////////////////////////////// profile page scene ->
    // profile fields:
    @FXML
    private Avatar avatar;
    @FXML
    private Label profileStatus;
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
    private HBox newPasswordHBox;
    @FXML
    private TextField newPasswordTextField;
    @FXML
    private Label changePasswordButton;
    @FXML
    private Label profileErrorMessage;
    @FXML
    private HBox changeStatusMenu;

    // profile methods:
    @FXML
    void editEnabledOrDone() throws IOException, ClassNotFoundException {
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
                String emptyMessage = "You haven't added a phone number yet.";
                boolean empty = profilePhoneNumber.getText().equals(emptyMessage) || profilePhoneNumber.getText().trim().equals("");
                boolean validPhoneNumber = (boolean) mySocket.sendSignalAndGetResponse(changeInfoAction) || empty;

                if (!validUsername) {
                    editErrorMessage.setText("Invalid username!");
                } else if (!validEmail) {
                    editErrorMessage.setText("Invalid email");
                } else if (!validPhoneNumber) {
                    editErrorMessage.setText("Invalid phone number (you can empty this field to remove your phone number)");
                } else {
                    profileUsername.setEditable(false);
                    profileEmail.setEditable(false);
                    profilePhoneNumber.setEditable(false);
                    editButton.setText("Edit");

                    String oldUsername = user.getUsername();
                    user.setUsername(profileUsername.getText());
                    user.setEmail(profileEmail.getText());
                    if (!empty) {
                        user.setPhoneNumber(profilePhoneNumber.getText());
                    } else {
                        user.setPhoneNumber(null);
                    }
                    if (empty) {
                        profilePhoneNumber.setText("You haven't added a phone number yet.");
                    }
                    boolean DBConnect = mySocket.sendSignalAndGetResponse(new UpdateUserOnMainServerAction(user, oldUsername));
                }
            }
        }
    }

    @FXML
    void changePassword(MouseEvent event) {
        switch (changePasswordButton.getText()) {
            case "Change Password" -> {
                newPasswordHBox.setVisible(true);
                newPasswordTextField.setEditable(true);
                changePasswordButton.setText("Cancel");
            }
            case "Cancel" -> doneWithPasswordChange();
        }
    }

    private void doneWithPasswordChange() {
        newPasswordTextField.setEditable(false);
        newPasswordHBox.setVisible(false);
        profileErrorMessage.setVisible(false);
        newPasswordTextField.setText("");
        changePasswordButton.setText("Change Password");
    }

    @FXML
    void doneChangingPassword(ActionEvent event) throws IOException, ClassNotFoundException {
        String newPassword = newPasswordTextField.getText().trim();
        SignUpOrChangeInfoAction changeInfoAction = new SignUpOrChangeInfoAction(user.getUsername());
        changeInfoAction.setPassword(newPassword);
        if (mySocket.sendSignalAndGetResponse(changeInfoAction)) {
            doneWithPasswordChange();
            user.setPassword(newPassword);
            boolean DBConnect = mySocket.sendSignalAndGetResponse(new UpdateUserOnMainServerAction(user));
        } else {
            profileErrorMessage.setVisible(true);
            profileErrorMessage.setText("Invalid format!");
        }
    }

    @FXML
    void editStatus(MouseEvent event) {
        changeStatusMenu.setVisible(true);
    }

    @FXML
    void setStatus(MouseEvent event) throws IOException, ClassNotFoundException {
        String newStatus = ((Label) event.getSource()).getText();
        switch (newStatus) {
            case "Online" -> user.setStatus(Status.Online);
            case "Idle" -> user.setStatus(Status.Idle);
            case "Do Not Disturb" -> user.setStatus(Status.DoNotDisturb);
            case "Invisible" -> user.setStatus(Status.Invisible);
        }
        setStatusLabel(user.getStatus());
        changeStatusMenu.setVisible(false);
        boolean DBConnect = mySocket.sendSignalAndGetResponse(new UpdateUserOnMainServerAction(user));
    }

    @FXML
    void changeAvatar(MouseEvent event) {

    }

    @FXML
    void enter(MouseEvent event) {

    }

    @FXML
    void logout(Event event) throws IOException, ClassNotFoundException {
        boolean DBConnect = getMySocket().sendSignalAndGetResponse(new LogoutAction(user));
        user = null;
        loadLoginMenu(event);
    }
}