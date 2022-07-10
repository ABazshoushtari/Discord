package discord.client;

import discord.signals.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;

import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
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
    private final SmartListener smartListener;

    // the constructor:
    public Controller(MySocket mySocket) {
        this.user = null;
        this.mySocket = mySocket;
        smartListener = new SmartListener(this);
        Thread smartListenerThread = new Thread(smartListener);
        smartListenerThread.setDaemon(true);
        smartListenerThread.start();
    }

    // getters and setters:
    public Model getUser() {
        return user;
    }

    public void setUser(Model user) {
        this.user = user;
    }

    public MySocket getMySocket() {
        return mySocket;
    }

    // some useful universal methods:
    private void waiting() {
        synchronized (this) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
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

    private String getAvatarImageCachePath(Asset asset) {
        makeDirectory("cache");
        if (asset instanceof Model model) {

            makeDirectory("cache" + File.separator + "User Profile Pictures");
            makeDirectory("cache" + File.separator + "User Profile Pictures" + File.separator + model.getUID());
            return "cache" + File.separator + "User Profile Pictures" + File.separator + model.getUID();
        } else if (asset instanceof Server server) {
            makeDirectory("cache" + File.separator + "server Profile Pictures");
            makeDirectory("cache" + File.separator + "server Profile Pictures" + File.separator + server.getUnicode());
            return "cache" + File.separator + "server Profile Pictures" + File.separator + server.getUnicode();
        }
        return null;
    }

    private Image readAvatarImage(Asset asset) throws IOException {
        if (asset.getAvatarImage() == null) {
            return new Image(getAbsolutePath("requirements" + File.separator + "emojipng.com-11701703.png"));
        }
        String directory = getAvatarImageCachePath(asset);
        FileOutputStream fos = new FileOutputStream(directory + File.separator + asset.getID() + "." + asset.getAvatarContentType());
        FileInputStream fis = new FileInputStream(directory + File.separator + asset.getID() + "." + asset.getAvatarContentType());
        fos.write(asset.getAvatarImage());
        Image avatarImage = new Image(fis);
        fos.close();
        fis.close();
        return avatarImage;
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
    void login(Event event) throws IOException {
        loginErrorMessage.setText("");
        String usernameField = usernameOnLoginMenu.getText();
        String passwordField = passwordOnLoginMenu.getText();
        if (!"".equals(usernameField.trim()) && !"".equals(passwordField.trim())) {
            mySocket.write(new LoginAction(usernameField, passwordField));
            waiting();
            user = smartListener.getReceivedUser();
            if (user == null) {
                loginErrorMessage.setText("A username by this password could not be found!");
            } else {
                //loadProfilePage(event);
                loadMainPage(event);
            }
        } else {
            loginErrorMessage.setText("You have empty fields!");
        }
    }

    private void loadProfilePage(Event event) throws IOException {
        loadScene(event, "ProfilePage.fxml");
//        if (user.getAvatarImage() != null) {
//            String directory = getAvatarImageCachePath(user);
//            try (FileOutputStream fileOutputStream = new FileOutputStream(directory + File.separator + user.getUID() + "." + user.getAvatarContentType());
//                 FileInputStream fileInputStream = new FileInputStream(directory + File.separator + user.getUID() + "." + user.getAvatarContentType())) {
//                fileOutputStream.write(user.getAvatarImage());
//                avatarImage = new Image(fileInputStream);
//            }
////            ByteArrayInputStream inStreambj = new ByteArrayInputStream(user.getAvatarImage());
////            Image newImage = ImageIO.read();
////            Image image = newImage();
////            avatar.setImage(newImage);
//            avatar.setFill(new ImagePattern(avatarImage));
//        }

        avatar.setFill(new ImagePattern(readAvatarImage(user)));
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

    @FXML
    void loadSignupMenu(Event event) {
        loadScene(event, "SignupMenu.fxml");
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
    void signup(Event event) throws IOException {

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
            mySocket.write(signupAction);
            waiting();
            Boolean success = smartListener.getReceivedBoolean();
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
            mySocket.write(signupAction);
            waiting();
            if (!smartListener.getReceivedBoolean()) {
                signupErrorMessage.setText("Invalid password format!");
                conditionMessage1.setText("A password should consist of only English letters/numbers and be of a minimal length of 8");
                conditionMessage2.setText("It should also at least have 1 small and 1 capital letter and 1 number");
                return;
            }

            // validating email
            signupAction.setEmail(emailField);
            mySocket.write(signupAction);
            waiting();
            if (!smartListener.getReceivedBoolean()) {
                signupErrorMessage.setText("Invalid email format!");
                return;
            }

            // no phone number is taken from the user at first
            signupAction.setPhoneNumber("");
            mySocket.write(signupAction);
            waiting();
            // a true is stored in receivedBoolean of the smart listener

            signupAction.finalizeStage();
            mySocket.write(signupAction);
            //waiting();
            //user = smartListener.getReceivedUser();     // we can get the signed-up user here but ignore for now
            loadLoginMenu(event);
            //loadProfilePage(event);
            //loadMainPage();
        }
    }

    @FXML
    void loadLoginMenu(Event event) {
        loadScene(event, "LoginMenu.fxml");
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
    void editEnabledOrDone() throws IOException {
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

                boolean validUsername;
                if (!user.getUsername().equals(profileUsername.getText())) {
                    mySocket.write(changeInfoAction);
                    waiting();
                    if (smartListener.getReceivedBoolean() == null) {
                        editErrorMessage.setText("This username is taken!");
                        return;
                    } else {
                        validUsername = smartListener.getReceivedBoolean();
                    }
                } else {
                    validUsername = true;
                }

                changeInfoAction.setEmail(profileEmail.getText());
                mySocket.write(changeInfoAction);
                waiting();
                boolean validEmail = smartListener.getReceivedBoolean();

                changeInfoAction.setUsername(profilePhoneNumber.getText());
                String emptyMessage = "You haven't added a phone number yet.";
                boolean empty = profilePhoneNumber.getText().equals(emptyMessage) || profilePhoneNumber.getText().trim().equals("");
                mySocket.write(changeInfoAction);
                waiting();
                boolean validPhoneNumber = smartListener.getReceivedBoolean() || empty;

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
    void doneChangingPassword(ActionEvent event) throws IOException {
        String newPassword = newPasswordTextField.getText().trim();
        SignUpOrChangeInfoAction changeInfoAction = new SignUpOrChangeInfoAction(user.getUsername());
        changeInfoAction.setPassword(newPassword);
        mySocket.write(changeInfoAction);
        waiting();
        if (smartListener.getReceivedBoolean()) {
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

        avatar.setFill(new ImagePattern(new Image(selectedFile.getAbsolutePath())));

//        BufferedImage image = ImageIO.read(selectedFile);
//        user.setAvatarImage(((DataBufferByte) image.getRaster().getDataBuffer()).getData());

//        ByteArrayOutputStream outStreamObj = new ByteArrayOutputStream();
        String[] parts = selectedFile.getName().split("\\.");
        user.setAvatarContentType(parts[parts.length - 1]);
//        ImageIO.write(image, parts[parts.length - 1], outStreamObj);
//        user.setAvatarImage(outStreamObj.toByteArray());

        String directory = getAvatarImageCachePath(user);
        try (FileOutputStream fileOutputStream = new FileOutputStream(directory + File.separator + user.getUID() + "." + user.getAvatarContentType());
             FileInputStream fileInputStream = new FileInputStream(selectedFile)) {
            user.setAvatarImage(fileInputStream.readAllBytes());
            fileOutputStream.write(user.getAvatarImage());
        }
        mySocket.write(new UpdateUserOnMainServerAction(user));
    }

    @FXML
    void removeAvatar() throws IOException {
        avatar.setFill(new Color(0.125, 0.13, 0, 0.145));
        user.setAvatarImage(null);
        mySocket.write(new UpdateUserOnMainServerAction(user));
    }

    @FXML
    void loadMainPage(Event event) throws IOException {
        loadScene(event, "MainPage.fxml");
        initializeMainPage();
    }

    @FXML
    void logout(Event event) throws IOException {
        mySocket.write(new LogoutAction());
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
    @FXML
    TabPane theTabPane;

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

    // pending:
    @FXML
    private ListView<Model> pendingListView;
    @FXML
    private Label pendingCount;

    // all friends:
    @FXML
    private ListView<Model> allListView;
    @FXML
    private Label allCount;

    // online friends:
    @FXML
    private ListView<Model> onlineListView;
    @FXML
    private Label onlineCount;

    // direct messages
    @FXML
    private VBox friendsDMVBox;
    @FXML
    private ListView<Model> directMessagesListView;

    // servers:
    @FXML
    private ListView<Server> serversListView;

    // main page methods:
    public void refreshBlockedPeople() throws IOException {
        //if (blockedListView == null) return;
        ObservableList<Model> blockedPeopleObservableList = FXCollections.observableArrayList();
        blockedListView.setStyle("-fx-background-color: #36393f");
        for (Integer UID : user.getBlockedList()) {
            mySocket.write(new GetUserFromMainServerAction(UID));
            waiting();
            Model blockedUser = smartListener.getReceivedUser();
            blockedPeopleObservableList.add(blockedUser);
        }
        blockedCount.setText("Blocked - " + user.getBlockedList().size());
        blockedListView.setItems(blockedPeopleObservableList);
    }

    public void refreshPending() throws IOException {
        // outgoing friend requests -> pending
        //if (pendingListView == null) return;
        ObservableList<Model> friendRequestsObservableList = FXCollections.observableArrayList();
        pendingListView.setStyle("-fx-background-color: #36393f");
        for (Integer UID : user.getIncomingFriendRequests()) {
            mySocket.write(new GetUserFromMainServerAction(UID));
            waiting();
            Model user = smartListener.getReceivedUser();
            friendRequestsObservableList.add(user);
        }
        pendingCount.setText("Pending - " + user.getIncomingFriendRequests().size());
        pendingListView.setItems(friendRequestsObservableList);
    }

    public void refreshAll() throws IOException {
        ObservableList<Model> allFriendsObservableList = FXCollections.observableArrayList();
        allListView.setStyle("-fx-background-color: #36393f");
        for (Integer UID : user.getFriends()) {
            mySocket.write(new GetUserFromMainServerAction(UID));
            waiting();
            Model friend = smartListener.getReceivedUser();
            allFriendsObservableList.add(friend);
        }
        allCount.setText("All - " + user.getFriends().size());
        allListView.setItems(allFriendsObservableList);
    }

    public void refreshOnline() throws IOException {
        ObservableList<Model> onlineFriendsObservableList = FXCollections.observableArrayList();
        onlineListView.setStyle("-fx-background-color: #36393f");
        int onlineFriendsCount = 0;
        for (Integer UID : user.getFriends()) {
            mySocket.write(new GetUserFromMainServerAction(UID));
            waiting();
            Model friend = smartListener.getReceivedUser();
            if (friend.getStatus() != Status.Invisible) {
                onlineFriendsObservableList.add(friend);
                onlineFriendsCount++;
            }
        }
        this.onlineCount.setText("Online - " + onlineFriendsCount);
        onlineListView.setItems(onlineFriendsObservableList);
    }

    public void refreshDirectMessages() throws IOException {
        ObservableList<Model> directMessagesObservableList = FXCollections.observableArrayList();
        directMessagesListView.setStyle("-fx-background-color: #2f3136");
        for (Integer UID : user.getFriends()) {
            mySocket.write(new GetUserFromMainServerAction(UID));
            waiting();
            Model friend = smartListener.getReceivedUser();
            directMessagesObservableList.add(friend);
        }
        directMessagesListView.setItems(directMessagesObservableList);
    }

    public void refreshFriends() throws IOException {
        //if (allListView == null || onlineListView == null || directMessagesListView == null) return;
        refreshAll();
        refreshOnline();
        refreshDirectMessages();
    }

    public void refreshServers() throws IOException {
        //if (serversListView == null) return;
        ObservableList<Server> serversObservableList = FXCollections.observableArrayList();
        serversListView.setStyle("-fx-background-color: #202225");
        for (Integer unicode : user.getServers()) {
            mySocket.write(new GetServerFromMainServerAction(unicode));
            waiting();
            Server server = smartListener.getReceivedServer();
            serversObservableList.add(server);
        }
        serversListView.setItems(serversObservableList);
    }

    public void setUpdatedValuesForObservableLists() throws IOException {
        refreshBlockedPeople();
        refreshPending();
        refreshFriends();
        refreshServers();
    }

    /*private GridPane getTabGridPane(int col2Width) {

        GridPane gridPane = new GridPane();

        gridPane.setStyle("-fx-background-color:  #36393f");

        ColumnConstraints col1 = new ColumnConstraints(USE_PREF_SIZE, USE_COMPUTED_SIZE, USE_PREF_SIZE);
        ColumnConstraints col2 = new ColumnConstraints(GridPane.USE_PREF_SIZE, col2Width, Double.MAX_VALUE);
        ColumnConstraints col3 = new ColumnConstraints(GridPane.USE_PREF_SIZE, GridPane.USE_COMPUTED_SIZE, GridPane.USE_PREF_SIZE);
        ColumnConstraints col4 = new ColumnConstraints(GridPane.USE_PREF_SIZE, GridPane.USE_COMPUTED_SIZE, GridPane.USE_PREF_SIZE);
        gridPane.getColumnConstraints().addAll(col1, col2, col3, col4);
        gridPane.setHgap(8);

        gridPane.setMinHeight(GridPane.USE_COMPUTED_SIZE);
        gridPane.setPrefHeight(GridPane.USE_COMPUTED_SIZE);
        gridPane.setMaxHeight(GridPane.USE_COMPUTED_SIZE);

        gridPane.setMinWidth(GridPane.USE_COMPUTED_SIZE);
        gridPane.setPrefWidth(GridPane.USE_COMPUTED_SIZE);
        gridPane.setMaxWidth(Double.MAX_VALUE);

        return gridPane;
    }*/

    public void initializeMainPage() throws IOException {

        setUpdatedValuesForObservableLists();

        initializeMyProfile();

        constructBlockedCells();
        constructPendingCells();
        constructAllFriendsCells();
        constructOnlineFriendsCells();
        constructDirectMessagesCells();
        constructServersCells();
    }

    private void initializeMyProfile() throws IOException {
        discordLogo.setFill(new ImagePattern(new Image(getAbsolutePath("requirements\\discordLogo.jpg"))));
        setting.setFill(new ImagePattern(new Image(getAbsolutePath("requirements\\user setting.jpg"))));

        mainPageAvatar.setFill(new ImagePattern(readAvatarImage(user)));

        usernameLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
        usernameLabel.setText(user.getUsername());
    }

    private void constructBlockedCells() {
        blockedListView.setCellFactory(blc -> new ListCell<>() {
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

                    gridPane.getColumnConstraints().addAll(col1, col2, col3);

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

                    try {
                        avatarPic.setFill(new ImagePattern(readAvatarImage(model)));
                    } catch (IOException e) {
                        e.printStackTrace();
                        avatarPic.setFill(new ImagePattern(new Image(getAbsolutePath("requirements" + File.separator + "emojipng.com-11701703.png"))));
                    }

                    username.setText(model.getUsername());

                    unblockButton.setOnAction(actionEvent -> {
                        user.getBlockedList().remove(model.getUID());
                        try {
                            mySocket.write(new UpdateUserOnMainServerAction(user));
                            setUpdatedValuesForObservableLists();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });

                    setGraphic(gridPane);
                }
            }
        });
    }

    private void constructPendingCells() {
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

                    try {
                        avatarPic.setFill(new ImagePattern(readAvatarImage(model)));
                    } catch (IOException e) {
                        e.printStackTrace();
                        avatarPic.setFill(new ImagePattern(new Image(getAbsolutePath("requirements" + File.separator + "emojipng.com-11701703.png"))));
                    }

                    username.setText(model.getUsername());
                    label.setText("incoming friend request");

                    acceptButton.setOnAction(actionEvent -> {
                        Integer UID = model.getUID();
                        int index = user.getIncomingFriendRequests().indexOf(UID);
                        try {
                            mySocket.write(new CheckFriendRequestsAction(user.getUID(), index, true));
                            waiting();
                            user = smartListener.getReceivedUser();
                            setUpdatedValuesForObservableLists();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });

                    rejectButton.setOnAction(actionEvent -> {
                        int index = user.getIncomingFriendRequests().indexOf(model.getUID());
                        try {
                            mySocket.write(new CheckFriendRequestsAction(user.getUID(), index, false));
                            waiting();
                            user = smartListener.getReceivedUser();
                            setUpdatedValuesForObservableLists();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });

                    setGraphic(gridPane);
                }
            }
        });
    }

    private void constructAllFriendsCells() {
        allListView.setCellFactory(fc -> new ListCell<>() {
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
                    enterChatButton.setStyle("-fx-background-color:  #3ca45c");
                    removeButton.setStyle("-fx-background-color:  #d83c3e");

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

                    try {
                        avatarPic.setFill(new ImagePattern(readAvatarImage(model)));
                    } catch (IOException e) {
                        e.printStackTrace();
                        avatarPic.setFill(new ImagePattern(new Image(getAbsolutePath("requirements" + File.separator + "emojipng.com-11701703.png"))));
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
                        int index = user.getFriends().indexOf(model.getUID());  // NECESSARY AND IMPORTANT for removing from directMessagesObservableList. 6 lines later
                                                                                // because now the model is different from the one saved in observableList
                                                                                // finglish: in model mal hamun listView hastesh ke az tush remove ro zadim, be hamin khater doroste ama baraye un yeki listView ok nist
                        user.removeFriend(model.getUID());
                        try {
                            mySocket.write(new RemoveFriendAction(user.getUID(), model.getUID()));
                            //waiting();
                            setUpdatedValuesForObservableLists();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });

                    setGraphic(gridPane);
                }
            }
        });
    }

    private void constructOnlineFriendsCells() {
        onlineListView.setCellFactory(ofc -> new ListCell<>() {
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
                    enterChatButton.setStyle("-fx-background-color:  #3ca45c");
                    removeButton.setStyle("-fx-background-color:  #d83c3e");

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

                    try {
                        avatarPic.setFill(new ImagePattern(readAvatarImage(model)));
                    } catch (IOException e) {
                        e.printStackTrace();
                        avatarPic.setFill(new ImagePattern(new Image(getAbsolutePath("requirements" + File.separator + "emojipng.com-11701703.png"))));
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
                        int index = user.getFriends().indexOf(model.getUID());  // NECESSARY AND IMPORTANT for removing from directMessagesObservableList. 6 lines later
                                                                                // because now the model is different from the one saved in observableList
                        user.removeFriend(model.getUID());
                        try {
                            mySocket.write(new RemoveFriendAction(user.getUID(), model.getUID()));
                            //waiting();
                            setUpdatedValuesForObservableLists();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });

                    setGraphic(gridPane);
                }
            }
        });
    }

    private void constructDirectMessagesCells() {
        directMessagesListView.setCellFactory(dmc -> new ListCell<>() {
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
                    Label status = new Label();

                    username.setStyle("-fx-font-weight: bold");
                    username.setStyle("-fx-font-size: 18");
                    username.setStyle("-fx-text-fill: White");

                    gridPane.setStyle("-fx-background-color: #2f3136");

                    ColumnConstraints col1 = new ColumnConstraints(USE_PREF_SIZE, USE_COMPUTED_SIZE, USE_PREF_SIZE);
                    ColumnConstraints col2 = new ColumnConstraints(GridPane.USE_PREF_SIZE, 105, Double.MAX_VALUE);
                    gridPane.getColumnConstraints().addAll(col1, col2);

                    gridPane.add(avatarPic, 0, 0, 1, GridPane.REMAINING);
                    gridPane.add(username, 1, 0, 1, 1);
                    gridPane.add(status, 1, 1, 1, 1);

                    gridPane.setHgap(8);

                    gridPane.setMinHeight(GridPane.USE_COMPUTED_SIZE);
                    gridPane.setPrefHeight(GridPane.USE_COMPUTED_SIZE);
                    gridPane.setMaxHeight(GridPane.USE_COMPUTED_SIZE);

                    gridPane.setMinWidth(GridPane.USE_COMPUTED_SIZE);
                    gridPane.setPrefWidth(GridPane.USE_COMPUTED_SIZE);
                    gridPane.setMaxWidth(Double.MAX_VALUE);

                    GridPane.setHalignment(avatarPic, HPos.LEFT);
                    GridPane.setHalignment(username, HPos.LEFT);

                    try {
                        avatarPic.setFill(new ImagePattern(readAvatarImage(model)));
                    } catch (IOException e) {
                        e.printStackTrace();
                        avatarPic.setFill(new ImagePattern(new Image(getAbsolutePath("requirements" + File.separator + "emojipng.com-11701703.png"))));
                    }

                    username.setText(model.getUsername());
                    status.setText(model.getStatus().toString());
                    switch (model.getStatus()) {
                        case Online -> status.setTextFill(new Color(0.24, 0.64, 0.36, 1));
                        case Idle -> status.setTextFill(new Color(0.98, 0.66, 0.1, 1));
                        case DoNotDisturb -> status.setTextFill(new Color(0.85, 0.24, 0.24, 1));
                        case Invisible -> status.setTextFill(new Color(0.4549, 0.498, 0.553, 1));
                    }

                    gridPane.setOnMouseClicked(mouseClickEvent -> enterChat(model.getUID()));

                    setGraphic(gridPane);
                }
            }
        });
    }

    private void constructServersCells() {
        serversListView.setCellFactory(sc -> new ListCell<>() {
            @Override
            protected void updateItem(Server server, boolean empty) {
                super.updateItem(server, empty);

                if (server == null || empty) {
                    setGraphic(null);
                } else {

                    // Variables (Controls; GUI components):
                    Rectangle avatarPic = new Rectangle(50, 50);

                    avatarPic.setStyle("-fx-arc-width: 200");
                    avatarPic.setStyle("-fx-arc-height: 200");

                    try {
                        avatarPic.setFill(new ImagePattern(readAvatarImage(server)));
                    } catch (IOException e) {
                        e.printStackTrace();
                        avatarPic.setFill(new ImagePattern(new Image(getAbsolutePath("requirements" + File.separator + "emojipng.com-11701703.png"))));
                    }

                    avatarPic.setOnMouseClicked(mouseClickEvent -> {
                        try {
                            loadServer(server);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });

                    setGraphic(avatarPic);
                }
            }
        });
    }

    @FXML
    void sendFriendRequest(ActionEvent event) throws IOException {
        String receivedUsername = friendRequestTextField.getText().trim();
        mySocket.write(new GetUIDbyUsernameAction(receivedUsername));
        waiting();
        Integer friendUID = smartListener.getReceivedInteger();
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
            mySocket.write(new SendFriendRequestAction(user.getUsername(), receivedUsername));
            waiting();
            int scenario = smartListener.getReceivedInteger();
            switch (scenario) {
                case 0 -> successOrError.setText("A user by this username was not found!");
                case 1 -> successOrError.setText("You have already sent a friend request to this user!");
                case 2 -> successOrError.setText("This user has blocked you! You can't send them a friend request");
                case 3 -> {
                    successOrError.setStyle("-fx-text-fill: #46C46E");
                    successOrError.setText("The request was sent successfully");
                }
            }
//            mySocket.write(new GetUserFromMainServerAction(user.getUID()));
//            waiting();
//            user = smartListener.getReceivedUser();
//            System.out.println(user.getIncomingFriendRequests().size());
        }
    }

    @FXML
    void enterChat(Integer friendUID) {

    }

    @FXML
    void createNewServer(Event event) {
        loadScene(event, "CreateNewServerPage.fxml");
    }

    @FXML
    void enterSetting(MouseEvent event) throws IOException, ClassNotFoundException {
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

    @FXML
    void loadHome() throws IOException {

        textChannelsVBox.setVisible(false);
        friendsDMVBox.setVisible(true);

        serverBorderPane.setVisible(false);
        theTabPane.setVisible(true);

        initializeMainPage();
    }

    @FXML
    void loadServer(Server server) throws IOException {

        friendsDMVBox.setVisible(false);
        textChannelsVBox.setVisible(true);

        theTabPane.setVisible(false);
        serverBorderPane.setVisible(true);

        initializeServerPage(server);
    }

    //////////////////////////////////////////////////////////// create new server scene ->
    @FXML
    private Circle newServerAvatarImage;
    @FXML
    private TextField newServerNameTextField;

    @FXML
    void addServerImage(MouseEvent event) {

    }

    @FXML
    void createServer(Event event) throws IOException {
        String newServerName = newServerNameTextField.getText().trim();
        if (!"".equals(newServerName)) {
            mySocket.write(new CreateNewServerAction());
            waiting();
            Integer newUnicode = smartListener.getReceivedInteger();
            Server newServer = new Server(newUnicode, newServerName, user.getUID());
            mySocket.write(new AddNewServerToDatabaseAction(newServer));
            //waiting();
            user.getServers().add(newUnicode);
            mySocket.write(new UpdateUserOnMainServerAction(user));
            //waiting();
            loadScene(event, "MainPage.fxml");
            initializeMyProfile();
            refreshServers();
            constructServersCells();
            loadServer(newServer);
        }
    }

    private void initializeServerPage(Server server) throws IOException {
        // set the variable in place
        middlePartUpperLabel.setText(server.getServerName());

        setUpdatedValuesForServerObservableLists(server, 0);

        // construct text channels cells:
        textChannelsListView.setCellFactory(tcc -> new ListCell<>() {
            @Override
            protected void updateItem(TextChannel textChannel, boolean empty) {

                super.updateItem(textChannel, empty);
                if (textChannel == null || empty) {
                    setGraphic(null);
                } else {

                    // Variables (Controls; GUI components):
                    Label textChannelName = new Label("# " + textChannel.getName());
                    textChannelName.setPadding(new Insets(5, 5, 5, 5));

                    textChannelName.setStyle("-fx-font-weight: bold");
                    textChannelName.setStyle("-fx-font-size: 18");
                    textChannelName.setStyle("-fx-text-fill: White");

                    textChannelName.setOnMouseClicked(mouseClickEvent -> refreshTextChannelChat(textChannel));

                    setGraphic(textChannelName);
                }
            }
        });

        // construct members cells:
        onlineMembersListView.setCellFactory(mc -> new ListCell<>() {
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
                    Label status = new Label();

                    username.setStyle("-fx-font-weight: bold");
                    username.setStyle("-fx-font-size: 18");
                    username.setStyle("-fx-text-fill: White");

                    gridPane.setStyle("-fx-background-color: #2f3136");

                    ColumnConstraints col1 = new ColumnConstraints(USE_PREF_SIZE, USE_COMPUTED_SIZE, USE_PREF_SIZE);
                    ColumnConstraints col2 = new ColumnConstraints(GridPane.USE_PREF_SIZE, GridPane.USE_COMPUTED_SIZE, Double.MAX_VALUE);
                    gridPane.getColumnConstraints().addAll(col1, col2);

                    gridPane.add(avatarPic, 0, 0, 1, GridPane.REMAINING);
                    gridPane.add(username, 1, 0, 1, 1);
                    gridPane.add(status, 1, 1, 1, 1);

                    gridPane.setHgap(8);

                    gridPane.setMinHeight(GridPane.USE_COMPUTED_SIZE);
                    gridPane.setPrefHeight(GridPane.USE_COMPUTED_SIZE);
                    gridPane.setMaxHeight(GridPane.USE_COMPUTED_SIZE);

                    gridPane.setMinWidth(GridPane.USE_COMPUTED_SIZE);
                    gridPane.setPrefWidth(GridPane.USE_COMPUTED_SIZE);
                    gridPane.setMaxWidth(GridPane.USE_COMPUTED_SIZE);

                    GridPane.setHalignment(avatarPic, HPos.LEFT);
                    GridPane.setHalignment(username, HPos.LEFT);

                    try {
                        avatarPic.setFill(new ImagePattern(readAvatarImage(model)));
                    } catch (IOException e) {
                        e.printStackTrace();
                        avatarPic.setFill(new ImagePattern(new Image(getAbsolutePath("requirements" + File.separator + "emojipng.com-11701703.png"))));
                    }

                    username.setText(model.getUsername());
                    status.setText(model.getStatus().toString());
                    switch (model.getStatus()) {
                        case Online -> status.setTextFill(new Color(0.24, 0.64, 0.36, 1));
                        case Idle -> status.setTextFill(new Color(0.98, 0.66, 0.1, 1));
                        case DoNotDisturb -> status.setTextFill(new Color(0.85, 0.24, 0.24, 1));
                        case Invisible -> status.setTextFill(new Color(0.4549, 0.498, 0.553, 1));
                    }

                    //gridPane.setOnMouseClicked(mouseClickEvent -> enterChat(model.getUID()));

                    setGraphic(gridPane);
                }
            }
        });
    }

    public void setUpdatedValuesForServerObservableLists(Server server, int textChannelIndex) throws IOException {
        refreshTextChannels(server);
        refreshTextChannelChat(server.getTextChannels().get(textChannelIndex));
        refreshMembers(server);
    }

    private void refreshTextChannels(Server server) {
        ObservableList<TextChannel> textChannelsObservableList = FXCollections.observableArrayList();
        textChannelsListView.setStyle("-fx-background-color:  #2f3136");
        textChannelsObservableList.addAll(server.getTextChannels());
        textChannelsListView.setItems(textChannelsObservableList);
    }

    private void refreshTextChannelChat(TextChannel textChannel) {
        ObservableList<TextChannelMessage> textChannelChatObservableList = FXCollections.observableArrayList();
        textChannelChatListView.setStyle("-fx-background-color: #36393F");
        textChannelChatObservableList.addAll(textChannel.getTextChannelMessages());
        textChannelChatListView.setItems(textChannelChatObservableList);
    }

    private void refreshMembers(Server server) throws IOException {

        ObservableList<Model> onlineMembersObservableList = FXCollections.observableArrayList();
        ObservableList<Model> offlineMembersObservableList = FXCollections.observableArrayList();

        onlineMembersListView.setStyle("-fx-background-color:   #2f3136");
        offlineMembersListView.setStyle("-fx-background-color:   #2f3136");

        int onlineCount = 0;
        int offlineCount = 0;

        for (Integer UID : server.getMembers().keySet()) {
            mySocket.write(new GetUserFromMainServerAction(UID));
            waiting();
            Model member = smartListener.getReceivedUser();
            if (member.getStatus() == Status.Invisible) {
                offlineMembersObservableList.add(member);
                offlineCount++;
            } else {
                onlineMembersObservableList.add(member);
                onlineCount++;
            }
        }

        onlineCountInServer.setText("Online - " + onlineCount);
        onlineMembersListView.setItems(onlineMembersObservableList);

        offlineCountInServer.setText("Offline - " + offlineCount);
        offlineMembersListView.setItems(offlineMembersObservableList);
    }

    //////////////////////////////////////////////////////////// server scene of the main page->
    // fields:
    @FXML
    private Button middlePartUpperLabel;
    @FXML
    private Label textChannelName;
    @FXML
    private VBox textChannelsVBox;
    @FXML
    private TextField textChannelMessage;
    @FXML
    private ListView<TextChannel> textChannelsListView;
    @FXML
    private ListView<TextChannelMessage> textChannelChatListView;
    @FXML
    private BorderPane serverBorderPane;
    @FXML
    private Label onlineCountInServer;
    @FXML
    private Label offlineCountInServer;
    @FXML
    private ListView<Model> onlineMembersListView;
    @FXML
    private ListView<Model> offlineMembersListView;

    // server scene methods:
    @FXML
    void addNewTextChannel() {

    }

    @FXML
    void sendFileOnTextChannel() {

    }
}