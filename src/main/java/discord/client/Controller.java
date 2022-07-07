package discord.client;

import discord.signals.*;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.imageio.ImageIO;

import javafx.scene.image.PixelFormat;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.*;

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

    private void loadProfilePage(Event event) throws IOException {
        loadScene(event, "profilePage.fxml");

        if (user.getAvatarImage() != null) {
            Image img;
            try (FileOutputStream fileOutputStream = new FileOutputStream("avatar." + user.getContentType());
                 FileInputStream fileInputStream = new FileInputStream("avatar." + user.getContentType())) {
                fileOutputStream.write(user.getAvatarImage());
                img = new Image(fileInputStream);
            }
//            ByteArrayInputStream inStreambj = new ByteArrayInputStream(user.getAvatarImage());
//            Image newImage = ImageIO.read();
//            Image image = newImage();
//            avatar.setImage(newImage);
            avatar.setImage(img); // Image object
        }
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
            case Online -> profileStatus.setFill(new Color(0, 1, 0, 1));
            case Idle -> profileStatus.setFill(new Color(1, 0.647, 0, 1));
            case DoNotDisturb -> profileStatus.setFill(new Color(1, 0, 0, 1));
            case Invisible -> profileStatus.setFill(new Color(0.502, 0.502, 0.502, 1));
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
    private ImageView avatar;
    @FXML
    private Circle profileStatus;
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
                    editErrorMessage.setText("");
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
    void changeAvatar(MouseEvent event) throws IOException, ClassNotFoundException {
//        Rectangle clip = new Rectangle(
//                avatar.getFitWidth(), avatar.getFitHeight()
//        );
//        clip.setArcWidth(1000);
//        clip.setArcHeight(1000);


//        avatar.setClip(clip);
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a profile pic");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("All Images", "*.jpg", "*.png"), new FileChooser.ExtensionFilter("JPG", "*.jpg"), new FileChooser.ExtensionFilter("PNG", "*.png"));
        File selectedFile = App.fileChooser(fileChooser);
        if (selectedFile == null) {
            return;
        }
        Image selectedImage = new Image(selectedFile.getAbsolutePath());
//        Circle circle = new Circle(65);
//        ImagePattern pattern = new ImagePattern(selectedImage);
//        circle.setFill(pattern);
//        avatar.setClip(clip);
        avatar.setImage(selectedImage);

//        BufferedImage image = ImageIO.read(selectedFile);
//        user.setAvatarImage(((DataBufferByte) image.getRaster().getDataBuffer()).getData());

//        ByteArrayOutputStream outStreamObj = new ByteArrayOutputStream();
        String[] parts = selectedFile.getName().split("\\.");
        user.setContentType(parts[parts.length - 1]);
//        ImageIO.write(image, parts[parts.length - 1], outStreamObj);
//        user.setAvatarImage(outStreamObj.toByteArray());

        try (FileInputStream fileInputStream = new FileInputStream(selectedFile)) {
            user.setAvatarImage(fileInputStream.readAllBytes());
        }
        boolean DBConnect = mySocket.sendSignalAndGetResponse(new UpdateUserOnMainServerAction(user));
    }

    @FXML
    void removeAvatar() throws IOException, ClassNotFoundException {
        avatar.setImage(null);
        user.setAvatarImage(null);
        boolean DBConnect = mySocket.sendSignalAndGetResponse(new UpdateUserOnMainServerAction(user));
    }

    @FXML
    void enter(Event event) {
        loadScene(event, "MainPage.fxml");
    }

    @FXML
    void logout(Event event) throws IOException, ClassNotFoundException {
        boolean DBConnect = getMySocket().sendSignalAndGetResponse(new LogoutAction(user));
        user = null;
        loadLoginMenu(event);
    }

    //////////////////////////////////////////////////////////// main page scene ->
    // main page fields:

    // main page methods:
}