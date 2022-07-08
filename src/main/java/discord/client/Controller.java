package discord.client;

import discord.signals.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;

import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

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

    private Image avatarImage;

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
            makeDirectory("Cache");
            makeDirectory("Cache" + File.separator + "User Profile Pictures");
            makeDirectory("Cache" + File.separator + "User Profile Pictures" + File.separator + user.getUID());
            String directory = "Cache" + File.separator + "User Profile Pictures" + File.separator + user.getUID();
            try (FileOutputStream fileOutputStream = new FileOutputStream(directory + File.separator + user.getUID() + "." + user.getAvatarContentType());
                 FileInputStream fileInputStream = new FileInputStream(directory + File.separator + user.getUID() + "." + user.getAvatarContentType())) {
                fileOutputStream.write(user.getAvatarImage());
                avatarImage = new Image(fileInputStream);
            }
//            ByteArrayInputStream inStreambj = new ByteArrayInputStream(user.getAvatarImage());
//            Image newImage = ImageIO.read();
//            Image image = newImage();
//            avatar.setImage(newImage);
            avatar.setFill(new ImagePattern(avatarImage));
        }

        profileUsername.setText(user.getUsername());
        profileEmail.setText(user.getEmail());
        setStatusColor(user.getPreviousSetStatus());
        if (user.getPhoneNumber() != null) {
            profilePhoneNumber.setText(user.getPhoneNumber());
        } else {
            profilePhoneNumber.setText("You haven't added a phone number yet.");
        }
    }

    private void setStatusColor(Status status) {
        switch (status) {
            case Online -> profileStatus.setFill(new Color(0.24, 0.64, 0.36, 1));
            case Idle -> profileStatus.setFill(new Color(0.98, 0.66, 0.1, 1));
            case DoNotDisturb -> profileStatus.setFill(new Color(0.85, 0.24, 0.24, 1));
            case Invisible -> profileStatus.setFill(new Color(0.4549, 0.498, 0.553, 1));
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
    private Circle avatar;
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
    private Button changePasswordButton;
    @FXML
    private Label profileErrorMessage;
    @FXML
    private HBox changeStatusMenu;

    // profile methods:
    @FXML
    void changeRedOnEnter(MouseEvent event) {
        Button redButton = (Button) event.getSource();
        redButton.setStyle("-fx-background-color: #A12D2F");
    }

    @FXML
    void changeRedOnExit(MouseEvent event) {
        Button redButton = (Button) event.getSource();
        redButton.setStyle("-fx-background-color:  #d83c3e");
    }

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
                    mySocket.write(new UpdateUserOnMainServerAction(user, oldUsername));
                }
            }
        }
    }

    @FXML
    void changePassword(ActionEvent event) {
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
            mySocket.write(new UpdateUserOnMainServerAction(user));
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
    void setStatus(MouseEvent event) throws IOException {
        String newStatus = ((Label) event.getSource()).getText();
        switch (newStatus) {
            case "Online" -> user.setStatus(Status.Online);
            case "Idle" -> user.setStatus(Status.Idle);
            case "Do Not Disturb" -> user.setStatus(Status.DoNotDisturb);
            case "Invisible" -> user.setStatus(Status.Invisible);
        }
        user.setPreviousSetStatus(user.getStatus());
        setStatusColor(user.getStatus());
        changeStatusMenu.setVisible(false);
        mySocket.write(new UpdateUserOnMainServerAction(user));
    }

    @FXML
    void changeAvatar(MouseEvent event) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a profile pic");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("All Images", "*.jpg", "*.jpeg", "*.png"), new FileChooser.ExtensionFilter("JPG", "*.jpg", "*.jpeg"), new FileChooser.ExtensionFilter("PNG", "*.png"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile == null) {
            return;
        }
        Image selectedImage = new Image(selectedFile.getAbsolutePath());
        avatarImage = selectedImage;
        avatar.setFill(new ImagePattern(selectedImage));

//        BufferedImage image = ImageIO.read(selectedFile);
//        user.setAvatarImage(((DataBufferByte) image.getRaster().getDataBuffer()).getData());

//        ByteArrayOutputStream outStreamObj = new ByteArrayOutputStream();
        String[] parts = selectedFile.getName().split("\\.");
        user.setAvatarContentType(parts[parts.length - 1]);
//        ImageIO.write(image, parts[parts.length - 1], outStreamObj);
//        user.setAvatarImage(outStreamObj.toByteArray());

        makeDirectory("Cache");
        makeDirectory("Cache" + File.separator + "User Profile Pictures");
        makeDirectory("Cache" + File.separator + "User Profile Pictures" + File.separator + user.getUID());
        String directory = "Cache" + File.separator + "User Profile Pictures" + File.separator + user.getUID();
        try (FileOutputStream fileOutputStream = new FileOutputStream(directory + File.separator + user.getUID() + "." + user.getAvatarContentType());
             FileInputStream fileInputStream = new FileInputStream(selectedFile)) {
            user.setAvatarImage(fileInputStream.readAllBytes());
            fileOutputStream.write(user.getAvatarImage());
        }
        mySocket.write(new UpdateUserOnMainServerAction(user));
    }

    @FXML
    void removeAvatar() throws IOException {
        avatar.setFill(null);
        user.setAvatarImage(null);
        mySocket.write(new UpdateUserOnMainServerAction(user));
    }

    @FXML
    void enter(Event event) throws IOException, ClassNotFoundException {
        loadScene(event, "MainPage.fxml");
        initializeMainPage();
    }

    @FXML
    void logout(Event event) throws IOException {
        getMySocket().write(new LogoutAction());
        user = null;
        loadLoginMenu(event);
    }

    //////////////////////////////////////////////////////////// main page scene ->
    // main page fields:

    @FXML
    private Rectangle discordLogo;

    @FXML
    private Circle mainPageAvatar;

    @FXML
    private Rectangle setting;

    @FXML
    private Label usernameLabel;

    // send a friend request:
    @FXML
    private TextField friendRequestTextField;
    @FXML
    private Label successOrError;

    // blocked:
    @FXML
    private ListView<Model> blockedListView;
    @FXML
    private Label blockedCount;
    private ObservableList<Model> blockedPeopleObservableList;

    // pending:
    @FXML
    private ListView<Model> pendingListView;
    @FXML
    private Label pendingCount;
    private ObservableList<Model> friendRequestsObservableList;

    // all friends:
    @FXML
    private ListView<Model> allListView;
    @FXML
    private Label allCount;
    private ObservableList<Model> allFriendsObservableList;

    // online friends:
    @FXML
    private ListView<Model> onlineListView;
    @FXML
    private Label onlineCount;
    private ObservableList<Model> onlineFriendsObservableList;

    private Image readProfileImage(Model model) throws IOException {
        makeDirectory("Cache");
        makeDirectory("Cache" + File.separator + "User Profile Pictures");
        makeDirectory("Cache" + File.separator + "User Profile Pictures" + File.separator + model.getUID());
        String directory = "Cache" + File.separator + "User Profile Pictures" + File.separator + model.getUID();
        FileOutputStream fos = new FileOutputStream(directory + File.separator + model.getUID() + "." + model.getAvatarContentType());
        FileInputStream fis = new FileInputStream(directory + File.separator + model.getUID() + "." + model.getAvatarContentType());
        fos.write(model.getAvatarImage());
        Image avatarImage = new Image(fis);
        fos.close();
        fis.close();
        return avatarImage;
    }

    // main page methods:
    public void initializeMainPage() throws IOException, ClassNotFoundException {

        blockedPeopleObservableList = FXCollections.observableArrayList();
        friendRequestsObservableList = FXCollections.observableArrayList();
        allFriendsObservableList = FXCollections.observableArrayList();
        onlineFriendsObservableList = FXCollections.observableArrayList();

        //discord logo:
        discordLogo.setFill(new ImagePattern(new Image(getAbsolutePath("requirements\\discordLogo.jpg"))));
        setting.setFill(new ImagePattern(new Image(getAbsolutePath("requirements\\user setting.jpg"))));
        mainPageAvatar.setFill(new ImagePattern(avatarImage));
        usernameLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
        usernameLabel.setText(user.getUsername());

        // blocked:
        blockedListView.setStyle("-fx-background-color:  #36393f");
        for (Integer UID : user.getBlockedList()) {
            Model blockedUser = mySocket.sendSignalAndGetResponse(new GetUserFromMainServerAction(UID));
            blockedPeopleObservableList.add(blockedUser);
        }
        blockedCount.setText("Blocked - " + user.getBlockedList().size());
        blockedListView.setItems(blockedPeopleObservableList);

        // pending:
        pendingListView.setStyle("-fx-background-color:  #36393f");
        for (Integer UID : user.getIncomingFriendRequests()) {
            Model user = mySocket.sendSignalAndGetResponse(new GetUserFromMainServerAction(UID));
            friendRequestsObservableList.add(user);
        }
        pendingCount.setText("Pending - " + user.getIncomingFriendRequests().size());
        pendingListView.setItems(friendRequestsObservableList);

        // all friends:
        allListView.setStyle("-fx-background-color:  #36393f");
        for (Integer UID : user.getFriends()) {
            Model friend = mySocket.sendSignalAndGetResponse(new GetUserFromMainServerAction(UID));
            allFriendsObservableList.add(friend);
        }
        allCount.setText("All - " + user.getFriends().size());
        allListView.setItems(allFriendsObservableList);

        // online friends:
        onlineListView.setStyle("-fx-background-color:  #36393f");
        int onlineFriendsCount = 0;
        for (Integer UID : user.getFriends()) {
            Model friend = mySocket.sendSignalAndGetResponse(new GetUserFromMainServerAction(UID));
            if (friend.getStatus() != Status.Invisible) {
                onlineFriendsObservableList.add(friend);
                onlineFriendsCount++;
            }
        }
        this.onlineCount.setText("Online - " + onlineFriendsCount);
        onlineListView.setItems(onlineFriendsObservableList);

        //construct blocked cells:
        blockedListView.setCellFactory(frc -> new ListCell<>() {
            @Override
            protected void updateItem(Model model, boolean empty) {
                super.updateItem(model, empty);

                if (model == null || empty) {
                    setGraphic(null);
                } else {
                    // Variables (Controls; GUI components):
                    GridPane gridPane = new GridPane();
                    Circle avatarPic = new Circle(20);
                    Label username = new Label();
                    Label label = new Label("Blocked");
                    Button unblockButton = new Button("Unblock");
                    // css styles
                    unblockButton.setStyle("-fx-background-color:  #d83c3e");

                    username.setStyle("-fx-font-weight: bold");
                    username.setStyle("-fx-font-size: 16");
                    username.setStyle("-fx-text-fill: White");

                    gridPane.setStyle("-fx-background-color:  #36393f");

                    // javafx codes. creating gridPane
                    ColumnConstraints col1 = new ColumnConstraints(USE_PREF_SIZE, USE_COMPUTED_SIZE, USE_PREF_SIZE);
                    ColumnConstraints col2 = new ColumnConstraints(GridPane.USE_PREF_SIZE, 300, Double.MAX_VALUE);
                    ColumnConstraints col3 = new ColumnConstraints(GridPane.USE_PREF_SIZE, GridPane.USE_COMPUTED_SIZE, GridPane.USE_PREF_SIZE);
                    ColumnConstraints col4 = new ColumnConstraints(GridPane.USE_PREF_SIZE, GridPane.USE_COMPUTED_SIZE, GridPane.USE_PREF_SIZE);
                    gridPane.getColumnConstraints().addAll(col1, col2, col3, col4);

                    gridPane.add(avatarPic, 0, 0, 1, GridPane.REMAINING);
                    gridPane.add(username, 1, 0, 1, 1);
                    gridPane.add(label, 1, 1, 1, 1);
                    gridPane.add(unblockButton, 2, 0, 1, GridPane.REMAINING);

                    gridPane.setHgap(8);

                    gridPane.setMinHeight(GridPane.USE_COMPUTED_SIZE);
                    gridPane.setPrefHeight(GridPane.USE_COMPUTED_SIZE);
                    gridPane.setMaxHeight(GridPane.USE_COMPUTED_SIZE);

                    gridPane.setMinWidth(GridPane.USE_COMPUTED_SIZE);
                    gridPane.setPrefWidth(GridPane.USE_COMPUTED_SIZE);
                    gridPane.setMaxWidth(Double.MAX_VALUE);

                    GridPane.setHalignment(avatarPic, HPos.LEFT);
                    GridPane.setHalignment(username, HPos.LEFT);
                    GridPane.setHalignment(unblockButton, HPos.LEFT);

                    if (model.getAvatarImage() != null) {
                        Image avatarImage;
                        try {
                            avatarImage = readProfileImage(model);
                            avatarPic.setFill(new ImagePattern(avatarImage));
                        } catch (IOException e) {
                            e.printStackTrace();
                            avatarPic.setStyle("-fx-background-color: BLACK");
                        }
                    } else {
                        avatarPic.setStyle("-fx-background-color: BLACK");
                    }

                    username.setText(model.getUsername());

                    unblockButton.setOnAction(actionEvent -> {
                        user.getBlockedList().remove(model.getUID());
                        blockedPeopleObservableList.remove(model);
                        blockedCount.setText("Blocked - " + user.getIncomingFriendRequests().size());
                        try {
                           mySocket.write(new UpdateUserOnMainServerAction(user));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });

                    setGraphic(gridPane);
                }
            }
        });

        // construct pending cells:
        pendingListView.setCellFactory(frc -> new ListCell<>() {
            @Override
            protected void updateItem(Model model, boolean empty) {
                super.updateItem(model, empty);

                if (model == null || empty) {
                    setGraphic(null);
                } else {
                    // Variables (Controls; GUI components):
                    GridPane gridPane = new GridPane();
                    Circle avatarPic = new Circle(20);
                    Label username = new Label();
                    Label label = new Label();
                    Button acceptButton = new Button("Accept");
                    Button rejectButton = new Button("Reject");
                    // css styles
                    acceptButton.setStyle("-fx-background-color:  #3ca45c");
                    rejectButton.setStyle("-fx-background-color:  #d83c3e");

                    username.setStyle("-fx-font-weight: bold");
                    username.setStyle("-fx-font-size: 16");
                    username.setStyle("-fx-text-fill: White");

                    label.setStyle("-fx-font-size: 14");
                    label.setStyle("-fx-text-fill: White");

                    gridPane.setStyle("-fx-background-color:  #36393f");

                    // javafx codes. creating gridPane for showing friend request
                    ColumnConstraints col1 = new ColumnConstraints(USE_PREF_SIZE, USE_COMPUTED_SIZE, USE_PREF_SIZE);
                    ColumnConstraints col2 = new ColumnConstraints(GridPane.USE_PREF_SIZE, 300, Double.MAX_VALUE);
                    ColumnConstraints col3 = new ColumnConstraints(GridPane.USE_PREF_SIZE, GridPane.USE_COMPUTED_SIZE, GridPane.USE_PREF_SIZE);
                    ColumnConstraints col4 = new ColumnConstraints(GridPane.USE_PREF_SIZE, GridPane.USE_COMPUTED_SIZE, GridPane.USE_PREF_SIZE);
                    gridPane.getColumnConstraints().addAll(col1, col2, col3, col4);

                    gridPane.add(avatarPic, 0, 0, 1, GridPane.REMAINING);
                    gridPane.add(username, 1, 0, 1, 1);
                    gridPane.add(label, 1, 1, 1, 1);
                    gridPane.add(acceptButton, 2, 0, 1, GridPane.REMAINING);
                    gridPane.add(rejectButton, 3, 0, 1, GridPane.REMAINING);

                    gridPane.setHgap(8);

                    gridPane.setMinHeight(GridPane.USE_COMPUTED_SIZE);
                    gridPane.setPrefHeight(GridPane.USE_COMPUTED_SIZE);
                    gridPane.setMaxHeight(GridPane.USE_COMPUTED_SIZE);

                    gridPane.setMinWidth(GridPane.USE_COMPUTED_SIZE);
                    gridPane.setPrefWidth(GridPane.USE_COMPUTED_SIZE);
                    gridPane.setMaxWidth(Double.MAX_VALUE);

                    GridPane.setHalignment(avatarPic, HPos.LEFT);
                    GridPane.setHalignment(username, HPos.LEFT);
                    GridPane.setHalignment(acceptButton, HPos.RIGHT);
                    GridPane.setHalignment(rejectButton, HPos.LEFT);

                    if (model.getAvatarImage() != null) {
                        Image avatarImage;
                        try {
                            avatarImage = readProfileImage(model);
                            avatarPic.setFill(new ImagePattern(avatarImage));
                        } catch (IOException e) {
                            e.printStackTrace();
                            avatarPic.setStyle("-fx-background-color: BLACK");
                        }
                    } else {
                        avatarPic.setStyle("-fx-background-color: BLACK");
                    }

                    username.setText(model.getUsername());
                    label.setText("incoming friend request");

                    acceptButton.setOnAction(actionEvent -> {
                        int index = user.getIncomingFriendRequests().indexOf(model.getUID());
                        try {
                            Boolean DBConnect = mySocket.sendSignalAndGetResponse(new CheckFriendRequestsAction(user.getUID(), index, true));
                            friendRequestsObservableList.remove(model);
                            user = mySocket.sendSignalAndGetResponse(new GetUserFromMainServerAction(user.getUID()));
                            pendingCount.setText("Pending - " + user.getIncomingFriendRequests().size());
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    });

                    rejectButton.setOnAction(actionEvent -> {
                        int index = user.getIncomingFriendRequests().indexOf(model.getUID());
                        try {
                            Boolean DBConnect = mySocket.sendSignalAndGetResponse(new CheckFriendRequestsAction(user.getUID(), index, false));
                            friendRequestsObservableList.remove(model);
                            user = mySocket.sendSignalAndGetResponse(new GetUserFromMainServerAction(user.getUID()));
                            pendingCount.setText("Pending - " +  user.getIncomingFriendRequests().size());
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    });

                    setGraphic(gridPane);
                }
            }
        });

        // construct all friends cells:
        allListView.setCellFactory(frc -> new ListCell<>() {
            @Override
            protected void updateItem(Model model, boolean empty) {
                super.updateItem(model, empty);

                if (model == null || empty) {
                    setGraphic(null);
                } else {
                    // Variables (Controls; GUI components):
                    GridPane gridPane = new GridPane();
                    Circle avatarPic = new Circle(20);
                    Label username = new Label();
                    Label label = new Label();
                    Button enterChatButton = new Button("Messages");
                    Button removeButton = new Button("Remove");
                    // css styles
                    //enterChatButton.setStyle("-fx-background-color:  #2F3136");
                    //removeButton.setStyle("-fx-background-color:  #2F3136");

                    username.setStyle("-fx-font-weight: bold");
                    username.setStyle("-fx-font-size: 16");
                    username.setStyle("-fx-text-fill: White");

                    label.setStyle("-fx-font-size: 14");
                    label.setStyle("-fx-text-fill: White");

                    gridPane.setStyle("-fx-background-color:  #36393f");

                    // javafx codes. creating gridPane for showing friend request
                    ColumnConstraints col1 = new ColumnConstraints(USE_PREF_SIZE, USE_COMPUTED_SIZE, USE_PREF_SIZE);
                    ColumnConstraints col2 = new ColumnConstraints(GridPane.USE_PREF_SIZE, 250, Double.MAX_VALUE);
                    ColumnConstraints col3 = new ColumnConstraints(GridPane.USE_PREF_SIZE, GridPane.USE_COMPUTED_SIZE, GridPane.USE_PREF_SIZE);
                    ColumnConstraints col4 = new ColumnConstraints(GridPane.USE_PREF_SIZE, GridPane.USE_COMPUTED_SIZE, GridPane.USE_PREF_SIZE);
                    gridPane.getColumnConstraints().addAll(col1, col2, col3, col4);

                    gridPane.add(avatarPic, 0, 0, 1, GridPane.REMAINING);
                    gridPane.add(username, 1, 0, 1, 1);
                    gridPane.add(label, 1, 1, 1, 1);
                    gridPane.add(enterChatButton, 2, 0, 1, GridPane.REMAINING);
                    gridPane.add(removeButton, 3, 0, 1, GridPane.REMAINING);

                    gridPane.setHgap(8);

                    gridPane.setMinHeight(GridPane.USE_COMPUTED_SIZE);
                    gridPane.setPrefHeight(GridPane.USE_COMPUTED_SIZE);
                    gridPane.setMaxHeight(GridPane.USE_COMPUTED_SIZE);

                    gridPane.setMinWidth(GridPane.USE_COMPUTED_SIZE);
                    gridPane.setPrefWidth(GridPane.USE_COMPUTED_SIZE);
                    gridPane.setMaxWidth(Double.MAX_VALUE);

                    GridPane.setHalignment(avatarPic, HPos.LEFT);
                    GridPane.setHalignment(username, HPos.LEFT);
                    GridPane.setHalignment(enterChatButton, HPos.RIGHT);
                    GridPane.setHalignment(removeButton, HPos.LEFT);


                    if (model.getAvatarImage() != null) {
                        Image avatarImage;
                        try {
                            avatarImage = readProfileImage(model);
                            avatarPic.setFill(new ImagePattern(avatarImage));
                        } catch (IOException e) {
                            e.printStackTrace();
                            avatarPic.setStyle("-fx-background-color: BLACK");
                        }
                    } else {
                        avatarPic.setStyle("-fx-background-color: BLACK");
                    }

                    username.setText(model.getUsername());

                    label.setText(model.getStatus().toString());
                    switch (model.getStatus()) {
                        case Online -> label.setTextFill(new Color(0.24, 0.64, 0.36, 1));
                        case Idle -> label.setTextFill(new Color(0.98, 0.66, 0.1, 1));
                        case DoNotDisturb -> label.setTextFill(new Color(0.85, 0.24, 0.24, 1));
                        case Invisible -> label.setTextFill(new Color(0.4549, 0.498, 0.553, 1));
                    }

                    enterChatButton.setOnAction(actionEvent -> enterChat(model.getUID()));

                    removeButton.setOnAction(actionEvent -> {
                        user.getFriends().remove(model.getUID());
                        allCount.setText("All - " + user.getFriends().size());
                        allFriendsObservableList.remove(model);
                        onlineFriendsObservableList.remove(model);
                        try {
                            boolean DBConnect = mySocket.sendSignalAndGetResponse(new RemoveFriendAction(user.getUID(), model.getUID()));
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    });

                    setGraphic(gridPane);
                }
            }
        });

        // construct online friends cell
        onlineListView.setCellFactory(frc -> new ListCell<>() {
            @Override
            protected void updateItem(Model model, boolean empty) {
                super.updateItem(model, empty);

                if (model == null || empty) {
                    setGraphic(null);
                } else {
                    // Variables (Controls; GUI components):
                    GridPane gridPane = new GridPane();
                    Circle avatarPic = new Circle(20);
                    Label username = new Label();
                    Label label = new Label();
                    Button enterChatButton = new Button("Messages");
                    Button removeButton = new Button("Remove");
                    // css styles
                    //enterChatButton.setStyle("-fx-background-color:  #2F3136");
                    //removeButton.setStyle("-fx-background-color:  #2F3136");

                    username.setStyle("-fx-font-weight: bold");
                    username.setStyle("-fx-font-size: 16");
                    username.setStyle("-fx-text-fill: White");

                    label.setStyle("-fx-font-size: 14");
                    label.setStyle("-fx-text-fill: White");

                    gridPane.setStyle("-fx-background-color:  #36393f");

                    ColumnConstraints col1 = new ColumnConstraints(USE_PREF_SIZE, USE_COMPUTED_SIZE, USE_PREF_SIZE);
                    ColumnConstraints col2 = new ColumnConstraints(GridPane.USE_PREF_SIZE, 250, Double.MAX_VALUE);
                    ColumnConstraints col3 = new ColumnConstraints(GridPane.USE_PREF_SIZE, GridPane.USE_COMPUTED_SIZE, GridPane.USE_PREF_SIZE);
                    ColumnConstraints col4 = new ColumnConstraints(GridPane.USE_PREF_SIZE, GridPane.USE_COMPUTED_SIZE, GridPane.USE_PREF_SIZE);
                    gridPane.getColumnConstraints().addAll(col1, col2, col3, col4);

                    gridPane.add(avatarPic, 0, 0, 1, GridPane.REMAINING);
                    gridPane.add(username, 1, 0, 1, 1);
                    gridPane.add(label, 1, 1, 1, 1);
                    gridPane.add(enterChatButton, 2, 0, 1, GridPane.REMAINING);
                    gridPane.add(removeButton, 3, 0, 1, GridPane.REMAINING);

                    gridPane.setHgap(8);

                    gridPane.setMinHeight(GridPane.USE_COMPUTED_SIZE);
                    gridPane.setPrefHeight(GridPane.USE_COMPUTED_SIZE);
                    gridPane.setMaxHeight(GridPane.USE_COMPUTED_SIZE);

                    gridPane.setMinWidth(GridPane.USE_COMPUTED_SIZE);
                    gridPane.setPrefWidth(GridPane.USE_COMPUTED_SIZE);
                    gridPane.setMaxWidth(Double.MAX_VALUE);

                    GridPane.setHalignment(avatarPic, HPos.LEFT);
                    GridPane.setHalignment(username, HPos.LEFT);
                    GridPane.setHalignment(enterChatButton, HPos.RIGHT);
                    GridPane.setHalignment(removeButton, HPos.LEFT);

                    if (model.getAvatarImage() != null) {
                        Image avatarImage;
                        try {
                            avatarImage = readProfileImage(model);
                            avatarPic.setFill(new ImagePattern(avatarImage));
                        } catch (IOException e) {
                            e.printStackTrace();
                            avatarPic.setStyle("-fx-background-color: BLACK");
                        }
                    } else {
                        avatarPic.setStyle("-fx-background-color: BLACK");
                    }

                    username.setText(model.getUsername());
                    label.setText(model.getStatus().toString());
                    switch (model.getStatus()) {
                        case Online -> label.setTextFill(new Color(0.24, 0.64, 0.36, 1));
                        case Idle -> label.setTextFill(new Color(0.98, 0.66, 0.1, 1));
                        case DoNotDisturb -> label.setTextFill(new Color(0.85, 0.24, 0.24, 1));
                        case Invisible -> label.setTextFill(new Color(0.4549, 0.498, 0.553, 1));
                    }

                    enterChatButton.setOnAction(actionEvent -> enterChat(model.getUID()));

                    removeButton.setOnAction(actionEvent -> {
                        user.getFriends().remove(model.getUID());
                        onlineCount.setText("Online - " + user.getIncomingFriendRequests().size());
                        onlineFriendsObservableList.remove(model);
                        allFriendsObservableList.remove(model);
                        try {
                            boolean DBConnect = mySocket.sendSignalAndGetResponse(new RemoveFriendAction(user.getUID(), model.getUID()));
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    });

                    setGraphic(gridPane);
                }
            }
        });

    }

    @FXML
    void sendFriendRequest(ActionEvent event) throws IOException, ClassNotFoundException {
        String receivedUsername = friendRequestTextField.getText().trim();
        Integer friendUID = mySocket.sendSignalAndGetResponse(new GetUIDbyUsernameAction(receivedUsername));
        successOrError.setStyle("-fx-text-fill: #E38082");
        if (friendUID == null) {
            successOrError.setText("A user by this username was not found!");
            return;
        }
        if (receivedUsername.length() > 0) {
            if (user.getUsername().equals(receivedUsername)) {
                successOrError.setText("You can't send a friend request to yourself!");
                return;
            }
            if (user.getFriends().contains(friendUID)) {
                successOrError.setText("This user is already your friend!");
                return;
            }
            if (user.getIncomingFriendRequests().contains(friendUID)) {
                successOrError.setText("Check your pending friend requests! :)");
                return;
            }
            int scenario = mySocket.sendSignalAndGetResponse(new SendFriendRequestAction(user.getUsername(), receivedUsername));
            switch (scenario) {
                case 0 -> successOrError.setText("A user by this username was not found!");
                case 1 -> successOrError.setText("You have already sent a friend request to this user!");
                case 2 -> successOrError.setText("This user has blocked you! You can't send them a friend request");
                case 3 -> successOrError.setText("Could not connect to the database!");
                case 4 -> {
                    successOrError.setStyle("-fx-text-fill: #46C46E");
                    successOrError.setText("The request was sent successfully");

//                    Model friend = mySocket.sendSignalAndGetResponse(new GetUserFromMainServerAction(receivedUsername));
//                    System.out.println(friend.getUID());
//                    friendRequestsObservableList.add(friend);
                }
            }
        }
    }

    @FXML
    void enterChat(Integer friendUID) {
    
    }

    @FXML
    void enterSetting(MouseEvent event) throws IOException {
        loadProfilePage(event);
    }

    @FXML
    void mouseEnteredSetting(MouseEvent event) {
        setting.setFill(new ImagePattern(new Image(getAbsolutePath("requirements\\user setting entered.jpg"))));
    }

    @FXML
    void mouseExitedSetting(MouseEvent event) {
        setting.setFill(new ImagePattern(new Image(getAbsolutePath("requirements\\user setting.jpg"))));
    }

    // some useful universal methods:
    private void makeDirectory(String path) {
        File directory = new File(path);
        if (!directory.exists()) {
            if (!directory.mkdir()) {
                System.err.println("Could not create the " + path + " directory!");
                throw new RuntimeException();
            }
        }
    }

    private String getAbsolutePath(String relativePath) {
        return new File("").getAbsolutePath() + File.separator + relativePath;
    }
}