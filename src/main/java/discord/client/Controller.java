package discord.client;

import discord.signals.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;

import javafx.scene.input.ContextMenuEvent;
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
import java.util.HashSet;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class Controller {

    // backend fields:
    private Model user;
    private Integer currentFriendDM;
    private Server currentServer;
    private TextChannel currentTextChannel;
    private final MySocket mySocket;
    private final SmartListener smartListener;

    //TODO check
    private ExecutorService executorService = Executors.newCachedThreadPool(new ThreadFactory() {
        public Thread newThread(Runnable r) {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setDaemon(true);
            return t;
        }
    });

    // ObservableLists:
    private ObservableList<Model> blockedPeopleObservableList;
    private ObservableList<Model> pendingObservableList;
    private ObservableList<Model> allFriendsObservableList;
    private ObservableList<Model> onlineFriendsObservableList;
    private ObservableList<Model> directMessagesObservableList;
    private ObservableList<Server> serversObservableList;
    private ObservableList<ChatMessage> chatMessageObservableList;
    private ObservableList<TextChannel> textChannelsObservableList;
    private ObservableList<TextChannelMessage> textChannelChatObservableList;
    private ObservableList<Model> onlineMembersObservableList;
    private ObservableList<Model> offlineMembersObservableList;

    public void addOrRemoveFromBlockedList(Model model, boolean add) {
        if (add) {
            blockedPeopleObservableList.add(model);
        } else {
            blockedPeopleObservableList.remove(model);
        }
        blockedCount.setText("Blocked - " + user.getBlockedList().size());
    }

    public void addOrRemoveFromPendingList(Model model, boolean add) {
        if (add) {
            pendingObservableList.add(model);
        } else {
            pendingObservableList.remove(model);
        }
        pendingCount.setText("Pending - " + (user.getSentFriendRequests().size() + user.getIncomingFriendRequests().size()));
    }

    public void addOrRemoveFromEveryFriendList(Model model, boolean add) {

        if (add) {
            allFriendsObservableList.add(model);
            onlineFriendsObservableList.add(model);
            directMessagesObservableList.add(model);
        } else {
            allFriendsObservableList.remove(model);
            onlineFriendsObservableList.remove(model);
            directMessagesObservableList.remove(model);
        }
        allCount.setText("All - " + user.getFriends().size());
        if (model.getStatus().equals(Status.Online)) {
            int change;
            if (add) {
                change = 1;
            } else {
                change = -1;
            }
            onlineCount.setText("Online - " + (Integer.parseInt(onlineCount.getText().split(" ")[2]) + change));
        }
    }

//    public void addOrRemoveFromServer(Model model, boolean add) {
//        int change;
//        if (add) {
//            change = 1;
//        } else {
//            change = -1;
//        }
//        if (model.getStatus().equals(Status.Invisible)) {
//            if (add) {
//                offlineMembersObservableList.add(model);
//            } else {
//                offlineMembersObservableList.remove(model);
//            }
//            offlineCountInServer.setText("Offline - " + (Integer.parseInt(offlineCountInServer.getText().split(" ")[3]) + change));
//        } else {
//            if (add) {
//                onlineMembersObservableList.add(model);
//            } else {
//                onlineMembersObservableList.remove(model);
//            }
//
//            onlineCountInServer.setText(("Online - " + (Integer.parseInt(onlineCountInServer.getText().split(" ")[3]) + change)));
//        }
//    }

    // getters of ObservableLists:
    public ObservableList<Model> getBlockedPeopleObservableList() {
        return blockedPeopleObservableList;
    }

    public ObservableList<Model> getPendingObservableList() {
        return pendingObservableList;
    }

    public ObservableList<Model> getAllFriendsObservableList() {
        return allFriendsObservableList;
    }

    public ObservableList<Model> getOnlineFriendsObservableList() {
        return onlineFriendsObservableList;
    }

    public ObservableList<Model> getDirectMessagesObservableList() {
        return directMessagesObservableList;
    }

    public ObservableList<Server> getServersObservableList() {
        return serversObservableList;
    }

    public ObservableList<ChatMessage> getChatMessageObservableList() {
        return chatMessageObservableList;
    }

    public ObservableList<ChatMessage> getTextChannelChatObservableList() {
        return textChannelChatObservableList;
    }

    public ObservableList<TextChannel> getTextChannelsObservableList() {
        return textChannelsObservableList;
    }

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

    public Server getCurrentServer() {
        return currentServer;
    }

    public TextChannel getCurrentTextChannel() {
        return currentTextChannel;
    }

    // some useful universal methods:
    private void writeAndWait(Action action) throws IOException {
        mySocket.write(action);
        waiting();
    }

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

    private void loadScene(String sceneName) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(sceneName));
        Stage stage = App.getStage();
        try {
            loader.setController(this);
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getAbsolutePath(String relativePath) {
        return new File("").getAbsolutePath() + File.separator + relativePath;
    }

    private Image readAvatarImage(byte[] imageBytes) throws IOException {
        if (imageBytes == null) {
            return new Image(getAbsolutePath("requirements" + File.separator + "emojipng.com-11701703.png"));
        }
        return new Image(new ByteArrayInputStream(imageBytes));
    }

    private Image readAvatarImage(Asset asset) throws IOException {
        if (asset.getAvatarImage() == null) {
            return new Image(getAbsolutePath("requirements" + File.separator + "emojipng.com-11701703.png"));
        }
        return new Image(new ByteArrayInputStream(asset.getAvatarImage()));
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
            writeAndWait(new LoginAction(usernameField, passwordField));
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
            writeAndWait(signupAction);
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
            writeAndWait(signupAction);
            if (!smartListener.getReceivedBoolean()) {
                signupErrorMessage.setText("Invalid password format!");
                conditionMessage1.setText("A password should consist of only English letters/numbers and be of a minimal length of 8");
                conditionMessage2.setText("It should also at least have 1 small and 1 capital letter and 1 number");
                return;
            }

            // validating email
            signupAction.setEmail(emailField);
            writeAndWait(signupAction);
            if (!smartListener.getReceivedBoolean()) {
                signupErrorMessage.setText("Invalid email format!");
                return;
            }

            // no phone number is taken from the user at first
            signupAction.setPhoneNumber("");
            writeAndWait(signupAction);
            // a true is stored in receivedBoolean of the smart listener

            signupAction.finalizeStage();
            writeAndWait(signupAction);
            loadLoginMenu(event);

            //user = smartListener.getReceivedUser();// we could also get the signed-up user here and load the main page
            //loadMainPage(event);
        }
    }

    @FXML
    void loadLoginMenu(Event event) {
        loadScene(event, "LoginMenu.fxml");
    }

    //////////////////////////////////////////////////////////// profile page scene ->
    // profile fields:
    @FXML
    private Label myAccountLabel;
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
    private Button changeAvatarButton;
    @FXML
    private Button removeAvatarButton;
    @FXML
    private Button editButton;
    @FXML
    private Label editErrorMessage;
    @FXML
    private HBox newPasswordHBox;
    @FXML
    private TextField newPasswordTextField;
    @FXML
    private Label profileErrorMessage;
    @FXML
    private HBox changeStatusMenu;
    @FXML
    private Button profileBackButton;
    @FXML
    private Button changePasswordButton;
    @FXML
    private Button logoutButton;

    // profile methods:
    @FXML
    void backFromProfile(Event event) throws IOException {
        switch (profileBackButton.getText()) {
            case "Main Page" -> loadMainPage(event);
            case "Server" -> {
                loadMainPage(event);
                loadServer();
            }
        }
    }

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
                    writeAndWait(changeInfoAction);
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
                writeAndWait(changeInfoAction);
                boolean validEmail = smartListener.getReceivedBoolean();

                changeInfoAction.setUsername(profilePhoneNumber.getText());
                String emptyMessage = "You haven't added a phone number yet.";
                boolean empty = profilePhoneNumber.getText().equals(emptyMessage) || profilePhoneNumber.getText().trim().equals("");
                writeAndWait(changeInfoAction);
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
                    writeAndWait(new UpdateUserOnMainServerAction(user, oldUsername));
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
        writeAndWait(changeInfoAction);
        if (smartListener.getReceivedBoolean()) {
            doneWithPasswordChange();
            user.setPassword(newPassword);
            writeAndWait(new UpdateUserOnMainServerAction(user));
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
        writeAndWait(new UpdateUserOnMainServerAction(user));
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

        String[] parts = selectedFile.getName().split("\\.");
        user.setAvatarContentType(parts[parts.length - 1]);

        try (FileInputStream fileInputStream = new FileInputStream(selectedFile)) {
            user.setAvatarImage(fileInputStream.readAllBytes());
        }
        writeAndWait(new UpdateUserOnMainServerAction(user));
    }

    @FXML
    void removeAvatar() throws IOException {
//        avatar.setFill(new Color(0.125, 0.13, 0, 0.145));
        avatar.setFill(new ImagePattern(new Image(getAbsolutePath("requirements" + File.separator + "emojipng.com-11701703.png"))));
        user.setAvatarImage(null);
        writeAndWait(new UpdateUserOnMainServerAction(user));
    }

    //TODO MOST IMPORTANT CHANGE
    @FXML
    void loadMainPage(Event event) throws IOException {
        loadScene(event, "MainPage.fxml");

        constructBlockedCells();
        constructPendingCells();
        constructOnlineOrAllCells(allListView);
        constructOnlineOrAllCells(onlineListView);
        constructDirectMessagesCells();
        constructServersCells();
        constructChatMessagesCells();

        discordLogo.setFill(new ImagePattern(new Image(getAbsolutePath("requirements\\discordLogo.jpg"))));
        setting.setFill(new ImagePattern(new Image(getAbsolutePath("requirements\\user setting.jpg"))));

        //TODO setListView
//        blockedListView.setItems(blockedPeopleObservableList);
//        pendingListView.setItems(pendingObservableList);
//        allListView.setItems(allFriendsObservableList);
//        onlineListView.setItems(onlineFriendsObservableList);
//        directMessagesListView.setItems(directMessagesObservableList);
//        serversListView.setItems(serversObservableList);
//        chatMessagesListView.setItems(chatMessageObservableList);

        refreshEverything();

        initializeMainPage();
    }

    public void loadMainPage() {

        loadScene("MainPage.fxml");
        constructBlockedCells();
        constructPendingCells();
        constructOnlineOrAllCells(allListView);
        constructOnlineOrAllCells(onlineListView);
        constructDirectMessagesCells();
        constructServersCells();
        constructChatMessagesCells();

        discordLogo.setFill(new ImagePattern(new Image(getAbsolutePath("requirements\\discordLogo.jpg"))));
        setting.setFill(new ImagePattern(new Image(getAbsolutePath("requirements\\user setting.jpg"))));

        try {
            refreshEverything();
            initializeMainPage();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void logout(Event event) throws IOException {
        mySocket.write(new LogoutAction());     // log out is the ONLY action that doesn't need waiting
        user = null;
        loadLoginMenu(event);
    }

    //////////////////////////////////////////////////////////// main page scene ->
    // main page fields:
    @FXML
    private TabPane theTabPane;
    @FXML
    private Label friendsLabel;
    @FXML
    private GridPane directMessageGridPane;
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

    // chat messages
    @FXML
    private TextField sendMessageTextField;
    @FXML
    private ListView<ChatMessage> chatMessagesListView;

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

        blockedPeopleObservableList = FXCollections.observableArrayList();
        blockedListView.setStyle("-fx-background-color: #36393f");
        for (Integer UID : user.getBlockedList()) {
            writeAndWait(new GetUserFromMainServerAction(UID));
            Model blockedUser = smartListener.getReceivedUser();
            blockedPeopleObservableList.add(blockedUser);
        }
        blockedCount.setText("Blocked - " + user.getBlockedList().size());
        blockedListView.setItems(blockedPeopleObservableList);
    }

    public void refreshPending() throws IOException {

        pendingObservableList = FXCollections.observableArrayList();

        pendingListView.setStyle("-fx-background-color: #36393f");

        // sent requests
        for (Integer UID : user.getSentFriendRequests()) {
            writeAndWait(new GetUserFromMainServerAction(UID));
            Model user = smartListener.getReceivedUser();
            pendingObservableList.add((user));
        }

        // incoming requests
        for (Integer UID : user.getIncomingFriendRequests()) {
            writeAndWait(new GetUserFromMainServerAction(UID));
            Model user = smartListener.getReceivedUser();
            pendingObservableList.add(user);
        }
        pendingCount.setText("Pending - " + (user.getSentFriendRequests().size() + user.getIncomingFriendRequests().size()));
        pendingListView.setItems(pendingObservableList);
    }

    public void refreshAll() throws IOException {
        allFriendsObservableList = FXCollections.observableArrayList();
        allListView.setStyle("-fx-background-color: #36393f");
        for (Integer UID : user.getFriends()) {
            writeAndWait(new GetUserFromMainServerAction(UID));
            Model friend = smartListener.getReceivedUser();
            allFriendsObservableList.add(friend);
        }
        allCount.setText("All - " + user.getFriends().size());
        allListView.setItems(allFriendsObservableList);
    }

    public void refreshOnline() throws IOException {
        onlineFriendsObservableList = FXCollections.observableArrayList();
        onlineListView.setStyle("-fx-background-color: #36393f");
        int onlineFriendsCount = 0;
        for (Integer UID : user.getFriends()) {
            writeAndWait(new GetUserFromMainServerAction(UID));
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
        directMessagesObservableList = FXCollections.observableArrayList();
        directMessagesListView.setStyle("-fx-background-color: #2f3136");
        for (Integer UID : user.getFriends()) {
            writeAndWait(new GetUserFromMainServerAction(UID));
            Model friend = smartListener.getReceivedUser();
            directMessagesObservableList.add(friend);
        }
        directMessagesListView.setItems(directMessagesObservableList);
    }

    public void refreshFriends() throws IOException {
        refreshAll();
        refreshOnline();
        refreshDirectMessages();
    }

    public void refreshServers() throws IOException {
        serversObservableList = FXCollections.observableArrayList();
        serversListView.setStyle("-fx-background-color: #202225");
        for (Integer unicode : user.getServers()) {
            writeAndWait(new GetServerFromMainServerAction(unicode));
            Server server = smartListener.getReceivedServer();
            serversObservableList.add(server);
        }
        serversListView.setItems(serversObservableList);
    }

    public void refreshEverything() throws IOException {
        refreshBlockedPeople();
        refreshPending();
        refreshFriends();
        refreshServers();
        refreshPrivateChat();
    }

    public void initializeMainPage() throws IOException {

        //TODO
//        refreshEverything();

        initializeMyProfile();

//        constructBlockedCells();
//        constructPendingCells();
//        constructOnlineOrAllCells(allListView);
//        constructOnlineOrAllCells(onlineListView);
//        constructDirectMessagesCells();
//        constructServersCells();

//        directMessagesListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Model>() {
//            @Override
//            public void changed(ObservableValue<? extends Model> observableValue, Model oldValue, Model newValue) {
//                currentFriendDM = newValue.getUID();
//                enterChat(newValue.getUsername());
//            }
//        });

//        constructChatMessagesCells();
    }

    private void initializeMyProfile() throws IOException {
        mainPageAvatar.setFill(new ImagePattern(readAvatarImage(user)));
        usernameLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
        usernameLabel.setText(user.getUsername());
        //discordLogo.setFill(new ImagePattern(new Image(getAbsolutePath("requirements\\discordLogo.jpg"))));
        //setting.setFill(new ImagePattern(new Image(getAbsolutePath("requirements\\user setting.jpg"))));
    }

    private void setUpGridPaneSizes(GridPane gridPane) {

        gridPane.setHgap(8);

        gridPane.setMinHeight(GridPane.USE_COMPUTED_SIZE);
        gridPane.setPrefHeight(GridPane.USE_COMPUTED_SIZE);
        gridPane.setMaxHeight(GridPane.USE_COMPUTED_SIZE);

        gridPane.setMinWidth(GridPane.USE_COMPUTED_SIZE);
        gridPane.setPrefWidth(GridPane.USE_COMPUTED_SIZE);
        gridPane.setMaxWidth(Double.MAX_VALUE);

        //return gridPane;
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

                    setUpGridPaneSizes(gridPane);

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
                            writeAndWait(new UpdateUserOnMainServerAction(user));
                            addOrRemoveFromBlockedList(model, false);
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

                    boolean outgoing = user.getSentFriendRequests().contains(model.getUID());

                    // Variables (Controls; GUI components):
                    GridPane gridPane = new GridPane();
                    Circle avatarPic = new Circle(20);
                    Label username = new Label();
                    Label label = new Label();
                    Button acceptButton = new Button("Accept");
                    Button ignoreOrCancelButton = new Button();

                    if (outgoing) {
                        ignoreOrCancelButton.setText("Cancel");
                    } else {
                        ignoreOrCancelButton.setText("Ignore");
                    }

                    // css styles
                    acceptButton.setStyle("-fx-background-color:  #3ca45c");
                    ignoreOrCancelButton.setStyle("-fx-background-color:  #d83c3e");

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
                    if (!outgoing) {
                        gridPane.add(acceptButton, 2, 0, 1, GridPane.REMAINING);
                    }
                    gridPane.add(ignoreOrCancelButton, 3, 0, 1, GridPane.REMAINING);

                    setUpGridPaneSizes(gridPane);

                    GridPane.setHalignment(avatarPic, HPos.LEFT);
                    GridPane.setHalignment(username, HPos.LEFT);
                    if (!outgoing) {
                        GridPane.setHalignment(acceptButton, HPos.RIGHT);
                    }
                    GridPane.setHalignment(ignoreOrCancelButton, HPos.LEFT);

                    try {
                        avatarPic.setFill(new ImagePattern(readAvatarImage(model)));
                    } catch (IOException e) {
                        e.printStackTrace();
                        avatarPic.setFill(new ImagePattern(new Image(getAbsolutePath("requirements" + File.separator + "emojipng.com-11701703.png"))));
                    }

                    username.setText(model.getUsername());
                    if (outgoing) {
                        label.setText("Outgoing Friend Request");
                    } else {
                        label.setText("incoming friend request");
                    }

                    acceptButton.setOnAction(actionEvent -> {
                        Integer UID = model.getUID();
                        int index = user.getIncomingFriendRequests().indexOf(UID);
                        try {
                            writeAndWait(new CheckFriendRequestsAction(user.getUID(), index, true));
                            user = smartListener.getReceivedUser();
                            addOrRemoveFromPendingList(model, false);
                            addOrRemoveFromEveryFriendList(model, true);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });

                    if (!outgoing) {    // ignore (reject) incoming request
                        ignoreOrCancelButton.setOnAction(actionEvent -> {
                            int index = user.getIncomingFriendRequests().indexOf(model.getUID());
                            try {
                                writeAndWait(new CheckFriendRequestsAction(user.getUID(), index, false));
                                user = smartListener.getReceivedUser();
                                addOrRemoveFromPendingList(model, false);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                    } else {    // cancel sent request
                        ignoreOrCancelButton.setOnAction(actionEvent -> {
                            try {
                                user.getSentFriendRequests().remove(model.getUID());
                                writeAndWait(new CancelSentFriendRequestAction(user.getUID(), model.getUID()));
                                addOrRemoveFromPendingList(model, false);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                    }

                    setGraphic(gridPane);
                }
            }
        });
    }

    private void constructOnlineOrAllCells(ListView<Model> modelListView) {
        modelListView.setCellFactory(ofc -> new ListCell<>() {
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

                    setUpGridPaneSizes(gridPane);

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

                    enterChatButton.setOnAction(actionEvent -> {
                        currentFriendDM = model.getUID();
                        enterChat(model.getUsername());
                    });

                    removeButton.setOnAction(actionEvent -> {
                        //int index = user.getFriends().indexOf(model.getChangerUserUID());  // NECESSARY AND IMPORTANT for removing from directMessagesObservableList. 6 lines later
                        // because now the model is different from the one saved in observableList
                        // finglish: in model mal hamun listView hastesh ke az tush remove ro zadim, be hamin khater doroste ama baraye un yeki listView ok nist
                        user.removeFriend(model.getUID());
                        try {
                            writeAndWait(new RemoveFriendAction(user.getUID(), model.getUID()));
                            addOrRemoveFromEveryFriendList(model, false);
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

                    GridPane gridPane = getUserGridPane(model);

                    gridPane.setOnMouseClicked(mouseClickEvent -> {
                        currentFriendDM = model.getUID();
                        enterChat(model.getUsername());
                    });

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
                    Circle avatarPic = new Circle(25);

                    try {
                        avatarPic.setFill(new ImagePattern(readAvatarImage(server)));
                    } catch (IOException e) {
                        e.printStackTrace();
                        avatarPic.setFill(new ImagePattern(new Image(getAbsolutePath("requirements" + File.separator + "emojipng.com-11701703.png"))));
                    }

                    avatarPic.setOnMouseClicked(mouseClickEvent -> {
                        try {
                            currentServer = server;
                            loadServer();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });

                    setGraphic(avatarPic);
                }
            }
        });
    }

    private void constructChatMessagesCells() {
        chatMessagesListView.setCellFactory(cmc -> new ListCell<>() {
            @Override
            protected void updateItem(ChatMessage chatMessage, boolean empty) {
                super.updateItem(chatMessage, empty);

                if (chatMessage == null || empty) {
                    setGraphic(null);
                } else {

                    // Variables (Controls; GUI components):
                    HBox hBox = new HBox(10);
                    Circle avatarPic = new Circle(20);
                    VBox vBox = new VBox(7);
                    HBox hBox2 = new HBox(10);
                    Label usernameLabel = new Label();
                    Label dateTimeLabel = new Label();
                    Label messageLabel = new Label();
                    Label editedLabel = new Label();
                    ContextMenu contextMenu = new ContextMenu();
                    CustomMenuItem reactionMenuItem;
                    HBox hBoxReaction = new HBox(10);
                    Circle laughReaction = new Circle(14);
                    Circle likeReaction = new Circle(14);
                    Circle dislikeReaction = new Circle(14);

                    // css styles
                    hBox.setStyle("-fx-background-color: #36393F");
                    hBox.setOnMouseEntered(new EventHandler<>() {
                        @Override
                        public void handle(MouseEvent mouseEvent) {
                            hBox.setStyle("-fx-background-color: #32353b");
                        }
                    });
                    hBox.setOnMouseExited(new EventHandler<>() {
                        @Override
                        public void handle(MouseEvent mouseEvent) {
                            hBox.setStyle("-fx-background-color: #36393F");
                        }
                    });

                    usernameLabel.setStyle("-fx-text-fill: White");

                    dateTimeLabel.setStyle("-fx-text-fill: #6f7681");

                    messageLabel.setStyle("-fx-background-color: #36393F");
                    messageLabel.setStyle("-fx-text-fill: white");
                    messageLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));

                    editedLabel.setStyle("-fx-text-fill: #6f7681");

//                    hBoxReaction.setStyle("-fx-background-color: #18191c");
                    laughReaction.setStyle("-fx-background-color: #202225");
//                    laughReaction.setStyle("-fx-background-size: 20");
                    likeReaction.setStyle("-fx-background-color: #202225");
//                    likeReaction.setStyle("-fx-background-size: cover");
                    dislikeReaction.setStyle("-fx-background-color: #202225");
//                    dislikeReaction.setStyle("-fx-background-size: auto");

                    // javafx codes.
                    hBox.setAlignment(Pos.TOP_LEFT);
                    hBox.setPadding((new Insets(10, 10, 0, 10)));
                    avatarPic.setStroke(Color.TRANSPARENT);
                    vBox.setAlignment(Pos.TOP_LEFT);
                    hBox2.setAlignment(Pos.CENTER_LEFT);
                    usernameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
                    dateTimeLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
                    editedLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
                    editedLabel.setPadding(new Insets(0, 0, 0, 10));

                    messageLabel.setMinWidth(USE_COMPUTED_SIZE);
                    messageLabel.setMinHeight(USE_COMPUTED_SIZE);
                    messageLabel.setPrefWidth(USE_COMPUTED_SIZE);
                    messageLabel.setPrefHeight(USE_COMPUTED_SIZE);
                    messageLabel.setMaxWidth(Double.MAX_VALUE);
                    messageLabel.setMaxHeight(USE_COMPUTED_SIZE);

                    hBox2.getChildren().addAll(usernameLabel, dateTimeLabel);

                    //reactionsMenuItem:
                    laughReaction.setFill(new ImagePattern(new Image(getAbsolutePath("requirements" + File.separator + "laugh emoji.png"))));
                    laughReaction.setOnMouseClicked(new EventHandler<>() {
                        @Override
                        public void handle(MouseEvent mouseEvent) {
                            chatMessage.laugh(user.getUID());
                            // send signal
                        }
                    });
                    likeReaction.setFill(new ImagePattern(new Image(getAbsolutePath("requirements" + File.separator + "like emoji.png"))));
                    likeReaction.setOnMouseClicked(new EventHandler<>() {
                        @Override
                        public void handle(MouseEvent mouseEvent) {
                            chatMessage.like(user.getUID());
                            // send signal
                        }
                    });
                    dislikeReaction.setFill(new ImagePattern(new Image(getAbsolutePath("requirements" + File.separator + "dislike emoji2.png"))));
                    dislikeReaction.setOnMouseClicked(new EventHandler<>() {
                        @Override
                        public void handle(MouseEvent mouseEvent) {
                            chatMessage.dislike(user.getUID());
                            // send signal
                        }
                    });
                    hBoxReaction.setAlignment(Pos.CENTER_LEFT);
                    hBoxReaction.getChildren().addAll(laughReaction, likeReaction, dislikeReaction);
                    reactionMenuItem = new CustomMenuItem(hBoxReaction);

                    try {
                        avatarPic.setFill(new ImagePattern(readAvatarImage(chatMessage.getSenderImage())));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    usernameLabel.setText(chatMessage.getSenderUsername());
                    dateTimeLabel.setText(chatMessage.getDateTime());
                    if (chatMessage.isEdited()) {
                        editedLabel.setText("(edited)");
                    }

                    // String chat Message
                    if (chatMessage instanceof ChatStringMessage chatStringMessage) {
                        messageLabel.setText(chatStringMessage.getMessage());
                        vBox.getChildren().addAll(hBox2, messageLabel, editedLabel);
                        MenuItem menuItemReactions = new MenuItem("Reactions");
                        MenuItem menuItemDeleteForMe = new MenuItem("Delete Message for me");
                        MenuItem menuItemDeleteForAll = new MenuItem("Delete Message for all");
                        if (chatMessage.getSenderUID().intValue() == user.getUID().intValue()) {
                            MenuItem menuItemEdit = new MenuItem("Edit Message");
                            /* setOnActions


                             */
                            contextMenu.getItems().addAll(reactionMenuItem, menuItemReactions, menuItemEdit, menuItemDeleteForMe, menuItemDeleteForAll);
                        } else {
                            contextMenu.getItems().addAll(reactionMenuItem, menuItemReactions, menuItemDeleteForMe, menuItemDeleteForAll);
                        }
                    } else {
                        messageLabel.setText(chatMessage.getMessage());
                        messageLabel.setStyle("-fx-text-fill: #3480eb");
                        if (chatMessage instanceof ChatFileMessage chatFileMessage) {
                            messageLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
                                @Override
                                public void handle(MouseEvent mouseEvent) {
                                    executorService.execute(new FileDownloader(user.getUsername(), chatFileMessage));
                                }
                            });
                        } else if (chatMessage instanceof ChatURLMessage chatURLMessage) {
                            messageLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
                                @Override
                                public void handle(MouseEvent mouseEvent) {
                                    executorService.execute(new HttpDownloader(user.getUsername(), chatURLMessage.getUrl(), chatURLMessage.getMessage()));
                                }
                            });
                        }
                        vBox.getChildren().addAll(hBox2, messageLabel, editedLabel);
                        MenuItem menuItemReactions = new MenuItem("Reactions");
                        MenuItem menuItemDeleteForMe = new MenuItem("Delete Message for me");
                        MenuItem menuItemDeleteForAll = new MenuItem("Delete Message for all");

                        contextMenu.getItems().addAll(reactionMenuItem, menuItemReactions, menuItemDeleteForMe, menuItemDeleteForAll);
                    }
                    hBox.getChildren().addAll(avatarPic, vBox);
                    hBox.setOnContextMenuRequested(new EventHandler<>() {
                        @Override
                        public void handle(ContextMenuEvent contextMenuEvent) {
                            contextMenu.show(hBox, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
                        }
                    });

                    setGraphic(hBox);
                }
            }
        });
    }

    private void constructTextChannelChatCells() {
        textChannelChatListView.setCellFactory(cmc -> new ListCell<>() {
            @Override
            protected void updateItem(ChatMessage chatMessage, boolean empty) {
                super.updateItem(chatMessage, empty);

                if (chatMessage == null || empty) {
                    setGraphic(null);
                } else {

                    // Variables (Controls; GUI components):
                    HBox hBox = new HBox(10);
                    Circle avatarPic = new Circle(20);
                    VBox vBox = new VBox(7);
                    HBox hBox2 = new HBox(10);
                    Label usernameLabel = new Label();
                    Label dateTimeLabel = new Label();
                    Label messageLabel = new Label();
                    Label editedLabel = new Label();
                    ContextMenu contextMenu = new ContextMenu();
                    CustomMenuItem reactionMenuItem;
                    HBox hBoxReaction = new HBox(10);
                    Circle laughReaction = new Circle(14);
                    Circle likeReaction = new Circle(14);
                    Circle dislikeReaction = new Circle(14);

                    // css styles
                    hBox.setStyle("-fx-background-color: #36393F");
                    hBox.setOnMouseEntered(new EventHandler<>() {
                        @Override
                        public void handle(MouseEvent mouseEvent) {
                            hBox.setStyle("-fx-background-color: #32353b");
                        }
                    });
                    hBox.setOnMouseExited(new EventHandler<>() {
                        @Override
                        public void handle(MouseEvent mouseEvent) {
                            hBox.setStyle("-fx-background-color: #36393F");
                        }
                    });

                    usernameLabel.setStyle("-fx-text-fill: White");

                    dateTimeLabel.setStyle("-fx-text-fill: #6f7681");

                    messageLabel.setStyle("-fx-background-color: #36393F");
                    messageLabel.setStyle("-fx-text-fill: white");
                    messageLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));

                    editedLabel.setStyle("-fx-text-fill: #6f7681");

//                    hBoxReaction.setStyle("-fx-background-color: #18191c");
                    laughReaction.setStyle("-fx-background-color: #202225");
//                    laughReaction.setStyle("-fx-background-size: 20");
                    likeReaction.setStyle("-fx-background-color: #202225");
//                    likeReaction.setStyle("-fx-background-size: cover");
                    dislikeReaction.setStyle("-fx-background-color: #202225");
//                    dislikeReaction.setStyle("-fx-background-size: auto");

                    // javafx codes.
                    hBox.setAlignment(Pos.TOP_LEFT);
                    hBox.setPadding((new Insets(10, 10, 0, 10)));
                    avatarPic.setStroke(Color.TRANSPARENT);
                    vBox.setAlignment(Pos.TOP_LEFT);
                    hBox2.setAlignment(Pos.CENTER_LEFT);
                    usernameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
                    dateTimeLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
                    editedLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
                    editedLabel.setPadding(new Insets(0, 0, 0, 10));

                    messageLabel.setMinWidth(USE_COMPUTED_SIZE);
                    messageLabel.setMinHeight(USE_COMPUTED_SIZE);
                    messageLabel.setPrefWidth(USE_COMPUTED_SIZE);
                    messageLabel.setPrefHeight(USE_COMPUTED_SIZE);
                    messageLabel.setMaxWidth(Double.MAX_VALUE);
                    messageLabel.setMaxHeight(USE_COMPUTED_SIZE);

                    hBox2.getChildren().addAll(usernameLabel, dateTimeLabel);

                    //reactionsMenuItem:
                    laughReaction.setFill(new ImagePattern(new Image(getAbsolutePath("requirements" + File.separator + "laugh emoji.png"))));
                    laughReaction.setOnMouseClicked(new EventHandler<>() {
                        @Override
                        public void handle(MouseEvent mouseEvent) {
                            chatMessage.laugh(user.getUID());
                            // send signal
                        }
                    });
                    likeReaction.setFill(new ImagePattern(new Image(getAbsolutePath("requirements" + File.separator + "like emoji.png"))));
                    likeReaction.setOnMouseClicked(new EventHandler<>() {
                        @Override
                        public void handle(MouseEvent mouseEvent) {
                            chatMessage.like(user.getUID());
                            // send signal
                        }
                    });
                    dislikeReaction.setFill(new ImagePattern(new Image(getAbsolutePath("requirements" + File.separator + "dislike emoji2.png"))));
                    dislikeReaction.setOnMouseClicked(new EventHandler<>() {
                        @Override
                        public void handle(MouseEvent mouseEvent) {
                            chatMessage.dislike(user.getUID());
                            // send signal
                        }
                    });
                    hBoxReaction.setAlignment(Pos.CENTER_LEFT);
                    hBoxReaction.getChildren().addAll(laughReaction, likeReaction, dislikeReaction);
                    reactionMenuItem = new CustomMenuItem(hBoxReaction);

                    try {
                        avatarPic.setFill(new ImagePattern(readAvatarImage(chatMessage.getSenderImage())));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    usernameLabel.setText(chatMessage.getSenderUsername());
                    dateTimeLabel.setText(chatMessage.getDateTime());
                    if (chatMessage.isEdited()) {
                        editedLabel.setText("(edited)");
                    }

                    // String chat Message
                    if (chatMessage instanceof ChatStringMessage chatStringMessage) {
                        messageLabel.setText(chatStringMessage.getMessage());
                        vBox.getChildren().addAll(hBox2, messageLabel, editedLabel);
                        MenuItem menuItemReactions = new MenuItem("Reactions");
                        MenuItem menuItemDeleteForMe = new MenuItem("Delete Message for me");
                        MenuItem menuItemDeleteForAll = new MenuItem("Delete Message for all");
                        if (chatMessage.getSenderUID().intValue() == user.getUID().intValue()) {
                            MenuItem menuItemEdit = new MenuItem("Edit Message");
                            /* setOnActions


                             */
                            contextMenu.getItems().addAll(reactionMenuItem, menuItemReactions, menuItemEdit, menuItemDeleteForMe, menuItemDeleteForAll);
                        } else {
                            contextMenu.getItems().addAll(reactionMenuItem, menuItemReactions, menuItemDeleteForMe, menuItemDeleteForAll);
                        }
                    } else {
                        messageLabel.setText(chatMessage.getMessage());
                        messageLabel.setStyle("-fx-text-fill: #3480eb");
                        if (chatMessage instanceof ChatFileMessage chatFileMessage) {
                            messageLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
                                @Override
                                public void handle(MouseEvent mouseEvent) {
                                    executorService.execute(new FileDownloader(user.getUsername(), chatFileMessage));
                                }
                            });
                        } else if (chatMessage instanceof ChatURLMessage chatURLMessage) {
                            messageLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
                                @Override
                                public void handle(MouseEvent mouseEvent) {
                                    executorService.execute(new HttpDownloader(user.getUsername(), chatURLMessage.getUrl(), chatURLMessage.getMessage()));
                                }
                            });
                        }
                        vBox.getChildren().addAll(hBox2, messageLabel, editedLabel);
                        MenuItem menuItemReactions = new MenuItem("Reactions");
                        MenuItem menuItemDeleteForMe = new MenuItem("Delete Message for me");
                        MenuItem menuItemDeleteForAll = new MenuItem("Delete Message for all");

                        contextMenu.getItems().addAll(reactionMenuItem, menuItemReactions, menuItemDeleteForMe, menuItemDeleteForAll);
                    }
                    hBox.getChildren().addAll(avatarPic, vBox);
                    hBox.setOnContextMenuRequested(new EventHandler<>() {
                        @Override
                        public void handle(ContextMenuEvent contextMenuEvent) {
                            contextMenu.show(hBox, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
                        }
                    });

                    setGraphic(hBox);
                }
            }
        });
    }

    @FXML
    void sendFriendRequest(ActionEvent event) throws IOException {
        String receivedUsername = friendRequestTextField.getText().trim();
        writeAndWait(new GetUIDbyUsernameAction(receivedUsername));
        Integer receiverUID = smartListener.getReceivedInteger();
        successOrError.setStyle("-fx-text-fill: #E38082");
        if (receiverUID == null) {
            successOrError.setText("A user by this username was not found!");
            return;
        }
        if (receivedUsername.length() > 0) {
            if (user.getUsername().equals(receivedUsername)) {
                successOrError.setText("You can't send a friend request to yourself!");
                return;
            }
            if (user.getFriends().contains(receiverUID)) {
                successOrError.setText("This user is already your friend!");
                return;
            }
            if (user.getIncomingFriendRequests().contains(receiverUID)) {
                successOrError.setText("Check your pending friend requests! :)");
                return;
            }
            writeAndWait(new SendFriendRequestAction(user.getUID(), receiverUID));
            Integer scenario = smartListener.getReceivedInteger();
            switch (scenario) {
                case 0 -> successOrError.setText("A user by this username was not found!");
                case 1 -> successOrError.setText("You have already sent a friend request to this user!");
                case 2 -> successOrError.setText("This user has blocked you! You can't send them a friend request");
                case 3 -> {    // the UID of the receiver user is sent back
                    successOrError.setStyle("-fx-text-fill: #46C46E");
                    successOrError.setText("The request was sent successfully");
                    user.getSentFriendRequests().add(receiverUID);
                    //writeAndWait(new UpdateUserOnMainServerAction(user));
                }
            }
            refreshPending();
            constructPendingCells();
        }
    }

    @FXML
    void enterChat(String friendName) {

//        currentFriendDM = friendUID;

        try {
            writeAndWait(new GetUserFromMainServerAction(user.getUID()));
            user = smartListener.getReceivedUser();
            user.enterPrivateChat(currentFriendDM);
            writeAndWait(new UpdateUserOnMainServerAction(user));
        } catch (IOException e) {
            e.printStackTrace();
        }

        friendNameLabel.setText(friendName);

//        ArrayList<Integer> receiver = new ArrayList<>();
//        receiver.add(currentFriendDM);
        sendMessageTextField.setUserData(currentFriendDM);

        constructChatMessagesCells();
        refreshPrivateChat();

        theTabPane.setVisible(false);
        serverBorderPane.setVisible(false);
        directMessageGridPane.setVisible(true);
    }

    public void refreshPrivateChat() {
        if (currentFriendDM == null) {
            return;
        }
        chatMessageObservableList = FXCollections.observableArrayList();
        if (user.getPrivateChats().get(currentFriendDM) != null) {  //
            chatMessageObservableList.addAll(user.getPrivateChats().get(currentFriendDM));
        }
        chatMessagesListView.setItems(chatMessageObservableList);
    }

    @FXML
    void loadCreateNewServerScene(Event event) {
        loadScene(event, "CreateOrEditServerPage.fxml");
        serverNameTextField.setText(user.getUsername() + "'s Server");
    }

    @FXML
    void loadProfile(MouseEvent event) throws IOException {

        permissionErrorMessage.setVisible(false);
        //user.getIsInChat().replace(currentFriendDM, false);
        user.makeAllIsInChatsFalse();
        writeAndWait(new UpdateUserOnMainServerAction(user));

        loadScene(event, "ProfilePage.fxml");
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

    private void loadProfile(Model user) throws IOException {

        loadScene("ProfilePage.fxml"); //TODO load profile
        avatar.setFill(new ImagePattern(readAvatarImage(user)));
        profileUsername.setText(user.getUsername());
        profileEmail.setText(user.getEmail());
        setStatusColor(user.getPreviousSetStatus());
        if (user.getPhoneNumber() != null) {
            profilePhoneNumber.setText(user.getPhoneNumber());
        } else {
            profilePhoneNumber.setText("This user hasn't added a phone number yet.");
        }

        myAccountLabel.setText(user.getUsername() + "'s Account");

        changePasswordButton.setVisible(false);
        logoutButton.setVisible(false);

        changeAvatarButton.setVisible(false);
        removeAvatarButton.setVisible(false);

        profileStatus.setDisable(true);
        editButton.setVisible(false);

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

        permissionErrorMessage.setVisible(false);
//        user.getIsInChat().replace(currentFriendDM, false);
        user.makeAllIsInChatsFalse();
        writeAndWait(new UpdateUserOnMainServerAction(user));

        textChannelsVBox.setVisible(false);
        friendsDMVBox.setVisible(true);

        serverBorderPane.setVisible(false);
        directMessageGridPane.setVisible(false);
        theTabPane.setVisible(true);

        initializeMainPage();
    }

    @FXML
    void loadServer() throws IOException {

        permissionErrorMessage.setVisible(false);
//        user.getIsInChat().replace(currentFriendDM, false);
        user.makeAllIsInChatsFalse();
        writeAndWait(new UpdateUserOnMainServerAction(user));

        friendsLabel.setVisible(false);
        serverMenuButton.setVisible(true);

        friendsDMVBox.setVisible(false);
        textChannelsVBox.setVisible(true);

        theTabPane.setVisible(false);
        directMessageGridPane.setVisible(false);
        serverBorderPane.setVisible(true);

        initializeServerPage();
    }

    //////////////////////////////////////////////////////////// direct message scene of the main page->
    // fields:
    @FXML
    private Label friendNameLabel;

    @FXML
    void sendPrivateChatMessage(ActionEvent event) throws IOException {
        TextField textField = (TextField) event.getSource();
        Integer friendUID = (Integer) textField.getUserData();
        ArrayList<Integer> friendUIDArrayList = new ArrayList<>();
        friendUIDArrayList.add(friendUID);

        ChatMessage chatMessage; // ChatStringMessage or ChatURLMessage

        String textFieldGetText = textField.getText();
        if (textFieldGetText.startsWith("/url ")) {
            URL url;
            try {
                url = new URL(textFieldGetText.split(" ")[1]);
                chatMessage = new ChatURLMessage(user.getUID(), friendUIDArrayList, -1, -1, false, url);
            } catch (MalformedURLException e) {
                textField.setText("Invalid Format! enter a url");
                textField.selectAll();
                textField.requestFocus();
                return;
            }
        } else {
            chatMessage = new ChatStringMessage(user.getUID(), friendUIDArrayList, -1, -1, false, textFieldGetText);
        }

        chatMessage.setSenderUsername(user.getUsername());
        chatMessage.setSenderImage(user.getAvatarImage());

        user.getPrivateChats().get(friendUID).add(chatMessage);
//        refreshPrivateChat();
        chatMessageObservableList.add(chatMessage);

        writeAndWait(chatMessage);
        textField.setText("");
    }
    
    @FXML
    void uploadPrivateChatFile(MouseEvent event) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a file to send");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("All Files", "*.*"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile == null) {
            return;
        }

        ChatFileMessage chatFileMessage;
        try (FileInputStream fileInputStream = new FileInputStream(selectedFile)) {
            ArrayList <Integer> receiver = new ArrayList<>();
            receiver.add(currentFriendDM);
            chatFileMessage = new ChatFileMessage(user.getUID(), receiver, -1, -1, false, selectedFile.getName(), fileInputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        chatFileMessage.setSenderUsername(user.getUsername());
        chatFileMessage.setSenderImage(user.getAvatarImage());

        user.getPrivateChats().get(currentFriendDM).add(chatFileMessage);
        chatMessageObservableList.add(chatFileMessage);

        writeAndWait(chatFileMessage);
    }

    //////////////////////////////////////////////////////////// create new server scene ->
    @FXML
    private Label createServerLabel1;
    @FXML
    private Label createServerLabel2;
    @FXML
    private Label createServerLabel3;
    @FXML
    private Circle serverAvatarImage;
    @FXML
    private TextField serverNameTextField;
    @FXML
    private Button createOrEditServerButton;
    @FXML
    private Button rolesButton;

    @FXML
    void addServerImage(MouseEvent event) throws IOException {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select an image to set for the server");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("All Images", "*.jpg", "*.jpeg", "*.png"), new FileChooser.ExtensionFilter("JPG", "*.jpg", "*.jpeg"), new FileChooser.ExtensionFilter("PNG", "*.png"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile == null) {
            return;
        }

        serverAvatarImage.setFill(new ImagePattern(new Image(selectedFile.getAbsolutePath())));

        try (FileInputStream fileInputStream = new FileInputStream(selectedFile)) {
            currentServer.setAvatarImage(fileInputStream.readAllBytes());
        }
        writeAndWait(new UpdateServerOnMainServerAction(currentServer));
    }

    @FXML
    void backFromCreateOrEditServer(Event event) throws IOException {
        loadMainPage(event);
        if ("Edit Your Server".equals(createServerLabel1.getText())) {
            loadServer();
        }
    }

    @FXML
    void createOrEditServer(Event event) throws IOException {
        String serverName = serverNameTextField.getText().trim();
        switch (createServerLabel1.getText()) {
            case "Create Your Server" -> {
                if (!"".equals(serverName)) {

                    writeAndWait(new CreateNewServerAction());
                    Integer newUnicode = smartListener.getReceivedInteger();
                    Server newServer = new Server(newUnicode, serverName, user.getUID());
                    writeAndWait(new AddNewServerToDatabaseAction(newServer));
                    user.getServers().add(newUnicode);
                    writeAndWait(new UpdateUserOnMainServerAction(user));

                    loadMainPage(event);

                    currentServer = newServer;
                    loadServer();
                }
            }
            case "Edit Your Server" -> {
                if (currentServer.getAllAbilities(user.getUID()).contains(Ability.ChangeServerName)) {
                    serverAvatarImage.setVisible(true);
                    currentServer.setServerName(serverName);
                    writeAndWait(new UpdateServerOnMainServerAction(currentServer));
                    loadMainPage(event);
                    loadServer();
                } else {
                    permissionErrorMessage.setVisible(true);
                }
            }
        }

    }

    @FXML
    private void loadRolesPage(Event event) {
        loadScene(event, "RolesPage.fxml");
    }

    private void initializeServerPage() throws IOException {
        serverMenuButton.setText(currentServer.getServerName());
        currentTextChannel = currentServer.getTextChannels().get(0);
        constructTextChannelsCells();
        constructTextChannelChatCells();
        constructMembersCells();

        refreshEveryServerThing();
    }

    public void refreshEveryServerThing() throws IOException {

        if (currentServer == null) return;

        permissionErrorMessage.setText("");
        serverMenuButton.setText(currentServer.getServerName());

        writeAndWait(new GetServerFromMainServerAction(currentServer.getUnicode()));
        currentServer = smartListener.getReceivedServer();

        refreshTextChannels();
        refreshTextChannelChat();
        refreshMembers();
    }

    public void refreshTextChannels() {
        textChannelsObservableList = FXCollections.observableArrayList();
        textChannelsListView.setStyle("-fx-background-color: #2f3136");
        textChannelsObservableList.addAll(currentServer.getTextChannels());
        textChannelsListView.setItems(textChannelsObservableList);
    }

    //TODO refreshTextChannelChat
    public void refreshTextChannelChat() {

        textChannelName.setText(currentTextChannel.getName());

        constructTextChannelChatCells();

        textChannelChatObservableList = FXCollections.observableArrayList();
        textChannelChatListView.setStyle("-fx-background-color: #36393F");

        // if not null:
        textChannelChatObservableList.addAll(currentTextChannel.getTextChannelMessages());

        textChannelChatListView.setItems(textChannelChatObservableList);
    }

    public void refreshMembers() throws IOException {

        onlineMembersObservableList = FXCollections.observableArrayList();
        offlineMembersObservableList = FXCollections.observableArrayList();

        onlineMembersListView.setStyle("-fx-background-color: #2f3136");
        offlineMembersListView.setStyle("-fx-background-color: #2f3136");

        int onlineCount = 0;
        int offlineCount = 0;

        for (Integer UID : currentServer.getMembers().keySet()) {
            writeAndWait(new GetUserFromMainServerAction(UID));
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

    private void constructTextChannelsCells() {
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

                    textChannelName.setOnMouseClicked(mouseClickEvent -> {
                        //TODO textChannel enter
                        currentTextChannel = textChannel;
                        enterTextChannelChat();
                    });

                    setGraphic(textChannelName);
                }
            }
        });
    }

    private void constructMembersCells() {
        constructMemberGridPaneCell(onlineMembersListView);
        constructMemberGridPaneCell(offlineMembersListView);
    }

    private void constructMemberGridPaneCell(ListView<Model> modelListView) {
        modelListView.setCellFactory(mc -> new ListCell<>() {
            @Override
            protected void updateItem(Model model, boolean empty) {
                super.updateItem(model, empty);
                if (model == null || empty) {
                    setGraphic(null);
                } else {
                    GridPane gridPane = getUserGridPane(model);
                    ContextMenu contextMenu = new ContextMenu();

                    MenuItem profile = new MenuItem("Profile");
                    MenuItem kick = new MenuItem("Kick");
                    //MenuItem addRole = new MenuItem("Add a role");

                    profile.setOnAction(new EventHandler<>() {
                        @Override
                        public void handle(ActionEvent event) {
                            try {
                                loadProfile(model);
                                profileBackButton.setText("Server");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    kick.setOnAction(new EventHandler<>() {
                        @Override
                        public void handle(ActionEvent event) {
                            if (currentServer.getAllAbilities(user.getUID()).contains(Ability.RemoveMember)) {
                                currentServer.getMembers().remove(model.getUID());
                                try {
                                    writeAndWait(new RemoveMemberFromServerAction(currentServer.getUnicode(), model.getUID()));
                                    if (model.getStatus().equals(Status.Invisible)) {
                                        offlineMembersObservableList.remove(model);
                                        int currentCount = Integer.parseInt(offlineCountInServer.getText().split(" ")[2]);
                                        offlineCountInServer.setText("Offline - " + (currentCount - 1));
                                    } else {
                                        onlineMembersObservableList.remove(model);
                                        int currentCount = Integer.parseInt(onlineCountInServer.getText().split(" ")[2]);
                                        onlineCountInServer.setText("Online - " + (currentCount - 1));
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                permissionErrorMessage.setVisible(true);
                            }
                        }
                    });

//                    addRole.setOnAction(new EventHandler<ActionEvent>() {
//                        @Override
//                        public void handle(ActionEvent event) {
//                            loadScene();
//                        }
//                    });

                    contextMenu.getItems().addAll(profile, kick);
                    gridPane.setOnContextMenuRequested(new EventHandler<>() {
                        @Override
                        public void handle(ContextMenuEvent contextMenuEvent) {
                            if (!model.getUID().equals(user.getUID())) {
                                contextMenu.show(gridPane, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
                            }
                        }
                    });

                    setGraphic(gridPane);
                }
            }
        });
    }

    private GridPane getUserGridPane(Model model) {

        GridPane gridPane = new GridPane();
        Circle avatarPic = new Circle(18);
        Label username = new Label();
        Label status = new Label();

        username.setStyle("-fx-font-weight: bold");
        username.setStyle("-fx-font-size: 12");
        username.setStyle("-fx-text-fill: White");

        gridPane.setStyle("-fx-background-color: #2f3136");

        ColumnConstraints col1 = new ColumnConstraints(GridPane.USE_PREF_SIZE, GridPane.USE_COMPUTED_SIZE, GridPane.USE_PREF_SIZE);
        ColumnConstraints col2 = new ColumnConstraints(GridPane.USE_PREF_SIZE, GridPane.USE_COMPUTED_SIZE, Double.MAX_VALUE);
        gridPane.getColumnConstraints().addAll(col1, col2);

        gridPane.add(avatarPic, 0, 0, 1, GridPane.REMAINING);
        gridPane.add(username, 1, 0, 1, 1);
        gridPane.add(status, 1, 1, 1, 1);

        setUpGridPaneSizes(gridPane);

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

        return gridPane;
    }


    //////////////////////////////////////////////////////////// server scene of the main page->
    // fields:
    @FXML
    private MenuButton serverMenuButton;
    @FXML
    private Label textChannelName;
    @FXML
    private TextField newTextChannelNameTextField;
    @FXML
    private VBox textChannelsVBox;
    @FXML
    private TextField textChannelMessage;
    @FXML
    private ListView<TextChannel> textChannelsListView;
    @FXML
    private ListView<ChatMessage> textChannelChatListView;
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
    @FXML
    private Label permissionErrorMessage;

    // server scene methods:
    @FXML
    void InvitePeople(Event event) throws IOException {
        loadScene("InvitePeopleScene.fxml");
        serverNameOnInviteScene.setText("Invite People to " + currentServer.getServerName());
        constructPeopleCells(true);
        refreshPeople();
    }

    // text channel chat
    private void enterTextChannelChat() {
        try {
            int index = currentServer.getTextChannels().indexOf(currentTextChannel);
            currentTextChannel.getMembers().replace(user.getUID(), true);
            // debug
            writeAndWait(new UpdateServerOnMainServerAction(currentServer));
            currentServer = smartListener.getReceivedServer();
            currentTextChannel = currentServer.getTextChannels().get(index);

//            writeAndWait(new GetUserFromMainServerAction(user.getUID()));
//            user = smartListener.getReceivedUser();
//            user.enterPrivateChat(currentFriendDM);
//            writeAndWait(new UpdateUserOnMainServerAction(user));
        } catch (IOException e) {
            e.printStackTrace();
        }

        ArrayList<Integer> receivers = new ArrayList<>(currentTextChannel.getMembers().keySet());
        sendMessageTextField.setUserData(receivers);

        refreshTextChannelChat();

//        theTabPane.setVisible(false);
//        serverBorderPane.setVisible(true);
//        directMessageGridPane.setVisible(false);
    }
    @FXML
    void sendTextChannelMessage(ActionEvent event) throws IOException {
        TextField textField = (TextField) event.getSource();
        ArrayList<Integer> friendUIDsArrayList = new ArrayList<>(currentTextChannel.getMembers().keySet());

        ChatMessage chatMessage; // ChatStringMessage or ChatURLMessage

        String textFieldGetText = textField.getText();
        if (textFieldGetText.startsWith("/url ")) {
            URL url;
            try {
                url = new URL(textFieldGetText.split(" ")[1]);
                chatMessage = new ChatURLMessage(user.getUID(), friendUIDsArrayList, currentServer.getUnicode(), currentTextChannel.getIndex(), true, url);
            } catch (MalformedURLException e) {
                textField.setText("Invalid Format! enter a url");
                textField.selectAll();
                textField.requestFocus();
                return;
            }
        } else {
            chatMessage = new ChatStringMessage(user.getUID(), friendUIDsArrayList, currentServer.getUnicode(), currentTextChannel.getIndex(), true, textFieldGetText);
        }
//        System.out.println(currentServer.getUnicode());

        chatMessage.setSenderUsername(user.getUsername());
        chatMessage.setSenderImage(user.getAvatarImage());

        currentTextChannel.getTextChannelMessages().add(chatMessage);
//        user.getPrivateChats().get(friendUID).add(chatMessage);
//        refreshPrivateChat();
        textChannelChatObservableList.add(chatMessage);

        writeAndWait(chatMessage);
        textField.setText("");
    }

    @FXML
    void uploadTextChannelFile(MouseEvent event) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a file to send");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("All Files", "*.*"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile == null) {
            return;
        }

        ChatFileMessage chatFileMessage;
        try (FileInputStream fileInputStream = new FileInputStream(selectedFile)) {
            ArrayList<Integer> receivers = new ArrayList<>(currentTextChannel.getMembers().keySet());
            chatFileMessage = new ChatFileMessage(user.getUID(), receivers, -1, -1, true, selectedFile.getName(), fileInputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        chatFileMessage.setSenderUsername(user.getUsername());
        chatFileMessage.setSenderImage(user.getAvatarImage());



        currentTextChannel.getTextChannelMessages().add(chatFileMessage);
//        user.getPrivateChats().get(currentFriendDM).add(chatFileMessage);
        textChannelChatObservableList.add(chatFileMessage);

        writeAndWait(chatFileMessage);
    }
    ////

    private void refreshPeople() throws IOException {
        ObservableList<Model> peopleObservableList = FXCollections.observableArrayList();
        uninvitedFriendsOrMembersListView.setStyle("-fx-background-color: #36393F");
        for (Integer UID : user.getFriends()) {
            if (currentServer.getMembers().containsKey(UID)) {
                continue;
            }
            writeAndWait(new GetUserFromMainServerAction(UID));
            Model friend = smartListener.getReceivedUser();
            peopleObservableList.add(friend);
        }
        uninvitedFriendsOrMembersListView.setItems(peopleObservableList);
    }

    private void constructPeopleCells(boolean invite) {
        uninvitedFriendsOrMembersListView.setCellFactory(mc -> new ListCell<>() {
            @Override
            protected void updateItem(Model model, boolean empty) {
                super.updateItem(model, empty);
                if (model == null || empty) {
                    setGraphic(null);
                } else {

                    GridPane gridPane = new GridPane();
                    Circle avatarPic = new Circle(20);
                    Label username = new Label();
                    Button inviteOrAddButton = new Button();
                    if (invite) inviteOrAddButton.setText("Invite");
                    else inviteOrAddButton.setText("Add");

                    username.setStyle("-fx-font-weight: bold");
                    username.setStyle("-fx-font-size: 18");
                    username.setStyle("-fx-text-fill: White");

                    gridPane.setStyle("-fx-background-color: #2f3136");

                    ColumnConstraints col1 = new ColumnConstraints(GridPane.USE_PREF_SIZE, GridPane.USE_COMPUTED_SIZE, GridPane.USE_PREF_SIZE);
                    ColumnConstraints col2 = new ColumnConstraints(GridPane.USE_PREF_SIZE, 880, Double.MAX_VALUE);
                    ColumnConstraints col3 = new ColumnConstraints(GridPane.USE_PREF_SIZE, GridPane.USE_COMPUTED_SIZE, GridPane.USE_PREF_SIZE);
                    gridPane.getColumnConstraints().addAll(col1, col2, col3);

                    gridPane.add(avatarPic, 0, 0, 1, GridPane.REMAINING);
                    gridPane.add(username, 1, 0, 1, GridPane.REMAINING);
                    gridPane.add(inviteOrAddButton, 2, 0, 1, GridPane.REMAINING);

                    setUpGridPaneSizes(gridPane);

                    GridPane.setHalignment(avatarPic, HPos.LEFT);
                    GridPane.setHalignment(username, HPos.LEFT);
                    GridPane.setHalignment(inviteOrAddButton, HPos.RIGHT);

                    try {
                        avatarPic.setFill(new ImagePattern(readAvatarImage(model)));
                    } catch (IOException e) {
                        e.printStackTrace();
                        avatarPic.setFill(new ImagePattern(new Image(getAbsolutePath("requirements" + File.separator + "emojipng.com-11701703.png"))));
                    }

                    username.setText(model.getUsername());

                    inviteOrAddButton.setOnAction(new EventHandler<>() {
                        @Override
                        public void handle(ActionEvent event) {
                            if (invite) {
                                if (currentServer.addNewMember(model.getUID())) {
                                    successOrFailMessageLabel.setText("Invited successfully!");
                                    inviteOrAddButton.setText("Done");
                                    try {
                                        writeAndWait(new UpdateServerOnMainServerAction(currentServer));
                                        writeAndWait(new AddFriendToServerAction(currentServer.getUnicode(), model.getUID()));
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    successOrFailMessageLabel.setStyle("-fx-text-fill: green");
                                } else {
                                    successOrFailMessageLabel.setText("Invited Failed! This user is banned from the server!");
                                    successOrFailMessageLabel.setStyle("-fx-text-fill: red");
                                }
                            } else {
                                Role currentRole = currentServer.getServerRoles().get(serverNameOnInviteScene.getText());
                                currentServer.getMembers().get(model.getUID()).add(currentRole);
                                try {
                                    writeAndWait(new UpdateServerOnMainServerAction(currentServer));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                inviteOrAddButton.setText("Done");
                            }
                            inviteOrAddButton.setDisable(true);
                        }
                    });

                    setGraphic(gridPane);
                }
            }
        });
    }

    @FXML
    void changeServerSettings() {
        loadScene("CreateOrEditServerPage.fxml");
        createServerLabel1.setText("Edit Your Server");
        createServerLabel2.setVisible(false);
        createServerLabel3.setVisible(false);
        createOrEditServerButton.setText("Edit");
        serverNameTextField.setText(currentServer.getServerName());
        rolesButton.setVisible(true);
        // role
        // limit members from the server
    }

    @FXML
    void createChannel() {
        if (currentServer.getAllAbilities(user.getUID()).contains(Ability.CreateChannel)) {
            newTextChannelNameTextField.setVisible(true);
            textChannelName.setText("Enter the name:");
        } else {
            permissionErrorMessage.setVisible(true);
        }
    }

    @FXML
    void createNewTextChannel() throws IOException {

        newTextChannelNameTextField.setVisible(false);
        textChannelName.setText(currentTextChannel.getName());

        String newTextChannelName = newTextChannelNameTextField.getText().trim();
        if (newTextChannelName.equals("")) return;

        TextChannel newTextChannel = currentServer.addNewTextChannel(newTextChannelName);
        writeAndWait(new UpdateServerOnMainServerAction(currentServer));

        int currentIndex = currentServer.getTextChannels().indexOf(currentTextChannel);
        currentTextChannel = currentServer.getTextChannels().get(currentIndex + 1);

        textChannelsObservableList.add(newTextChannel);
        textChannelName.setText(currentTextChannel.getName());

        refreshTextChannelChat();
    }

    //////////////////////////////////////////////////////////// invite people scene ->
    // fields:
    @FXML
    private Label serverNameOnInviteScene;
    @FXML
    private Label successOrFailMessageLabel;
    @FXML
    private ListView<Model> uninvitedFriendsOrMembersListView;

    // methods:
    @FXML
    private void backFromInvitePeople(Event event) throws IOException {
        loadMainPage(event);
        loadServer();
    }


    //////////////////////////////////////////////////////////// create new role scene ->
    // fields:
    @FXML
    private TextField newRoleNameTextField;

    @FXML
    private CheckBox banAbility;

    @FXML
    private CheckBox changeServerNameAbility;

    @FXML
    private CheckBox createChannelAbility;

    @FXML
    private CheckBox limitMembersOfChannelsAbility;

    @FXML
    private CheckBox pinMessageAbility;

    @FXML
    private CheckBox removeChannelAbility;

    @FXML
    private CheckBox removeMemberAbility;

    @FXML
    private CheckBox seeChatHistoryAbility;

    @FXML
    void doneMakingNewRole(Event event) throws IOException {

        String newRoleName = newRoleNameTextField.getText().trim();

        HashSet<Ability> newRoleAbilities = new HashSet<>();
        if (banAbility.isSelected()) newRoleAbilities.add(Ability.Ban);
        if (changeServerNameAbility.isSelected()) newRoleAbilities.add(Ability.ChangeServerName);
        if (createChannelAbility.isSelected()) newRoleAbilities.add(Ability.CreateChannel);
        if (limitMembersOfChannelsAbility.isSelected()) newRoleAbilities.add(Ability.LimitMembersOfChannels);
        if (pinMessageAbility.isSelected()) newRoleAbilities.add(Ability.PinMessage);
        if (removeChannelAbility.isSelected()) newRoleAbilities.add(Ability.RemoveChannel);
        if (removeMemberAbility.isSelected()) newRoleAbilities.add(Ability.RemoveMember);
        if (seeChatHistoryAbility.isSelected()) newRoleAbilities.add(Ability.SeeChatHistory);

        Role newRole = new Role(newRoleName, newRoleAbilities);
        currentServer.getServerRoles().put(newRoleName, newRole);
        writeAndWait(new UpdateServerOnMainServerAction(currentServer));

        loadScene(event, "InvitePeopleScene.fxml");
        serverNameOnInviteScene.setText(newRoleName);
        constructPeopleCells(false);
        refreshFutureRoleHolders();
        //loadMainPage(event);
        //loadServer();
    }

    private void refreshFutureRoleHolders() throws IOException {
        ObservableList<Model> peopleObservableList = FXCollections.observableArrayList();
        uninvitedFriendsOrMembersListView.setStyle("-fx-background-color: #36393F");
        for (Integer UID : currentServer.getMembers().keySet()) {
            writeAndWait(new GetUserFromMainServerAction(UID));
            Model member = smartListener.getReceivedUser();
            peopleObservableList.add(member);
        }
        uninvitedFriendsOrMembersListView.setItems(peopleObservableList);
    }
}