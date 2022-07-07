package discord.client;

import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;

import java.io.*;

public class FriendRequestCell extends ListCell<Model> {
    // Fields:
    private final GridPane gridPane = new GridPane();
    private final Circle avatarPic = new Circle(20);
    private final Label username = new Label();
    private final Label status = new Label();
    private final Button acceptButton;
    private final Button rejectButton;

    // Constructors:
    public FriendRequestCell(Button acceptButton, Button rejectButton) {
        this.acceptButton = acceptButton;
        this.rejectButton = rejectButton;
        acceptButton.setStyle("-fx-background-color:  #3ca45c");
        rejectButton.setStyle("-fx-background-color:  #d83c3e");

        username.setStyle("-fx-font-weight: bold");
        username.setStyle("-fx-font-size: 16");
        username.setStyle("-fx-text-fill: White");

        status.setStyle("-fx-font-size: 14");
        status.setStyle("-fx-text-fill: White");

        gridPane.setStyle("-fx-background-color:  #36393f");

//        gridPane.add(avatarPic);
        ColumnConstraints col1 = new ColumnConstraints(USE_PREF_SIZE, USE_COMPUTED_SIZE, USE_PREF_SIZE);
        ColumnConstraints col2 = new ColumnConstraints(GridPane.USE_PREF_SIZE, 300, Double.MAX_VALUE);
        ColumnConstraints col3 = new ColumnConstraints(GridPane.USE_PREF_SIZE, GridPane.USE_COMPUTED_SIZE, GridPane.USE_PREF_SIZE);
        ColumnConstraints col4 = new ColumnConstraints(GridPane.USE_PREF_SIZE, GridPane.USE_COMPUTED_SIZE, GridPane.USE_PREF_SIZE);
        gridPane.getColumnConstraints().addAll(col1, col2, col3, col4);

        gridPane.add(avatarPic, 0, 0, 1, GridPane.REMAINING);
        gridPane.add(username, 1, 0, 1, 1);
        gridPane.add(status, 1, 1, 1, 1);
        gridPane.add(acceptButton, 2, 0, 1, GridPane.REMAINING);
        gridPane.add(rejectButton, 3, 0, 1, GridPane.REMAINING);
//        GridPane.setConstraints(avatarPic, 0, 0);
//        GridPane.setConstraints(username, 1, 0);
//        GridPane.setConstraints(status, 1, 1);
//        GridPane.setConstraints(acceptButton, 2, 0);
//        GridPane.setConstraints(rejectButton, 3, 0);


        gridPane.setHgap(8);
//        gridPane.setAlignment(Pos.CENTER);

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

//        gridPane.getChildren().addAll(avatarPic, username, acceptButton, rejectButton);
    }

    // Methods:
    // Getters:
    public Button getAcceptButton() {
        return acceptButton;
    }

    public Button getRejectButton() {
        return rejectButton;
    }

    // Other Methods:
    @Override
    protected void updateItem(Model model, boolean empty) {
        super.updateItem(model, empty);

        if (model == null || empty) {
            setGraphic(null);
        } else {
            if (model.getAvatarImage() != null) {
                Image avatarImage = null;
                makeDirectory("Cache");
                makeDirectory("Cache" + File.separator + "User Profile Pictures");
                makeDirectory("Cache" + File.separator + "User Profile Pictures" + File.separator + model.getUID());
                String directory = "Cache" + File.separator + "User Profile Pictures" + File.separator + model.getUID();
                try (FileOutputStream fileOutputStream = new FileOutputStream(directory + File.separator + model.getUID() + "." + model.getAvatarContentType());
                     FileInputStream fileInputStream = new FileInputStream(directory + File.separator + model.getUID() + "." + model.getAvatarContentType())) {
                    fileOutputStream.write(model.getAvatarImage());
                    avatarImage = new Image(fileInputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                avatarPic.setFill(new ImagePattern(avatarImage));
            } else {
                avatarPic.setStyle("-fx-background-color: BLACK");
            }

            username.setText(model.getUsername());
            status.setText("incoming friend request");
            setGraphic(gridPane);
        }
    }

    private void makeDirectory(String path) {
        File directory = new File(path);
        if (!directory.exists()) {
            if (!directory.mkdir()) {
                //printer.printErrorMessage("Could not create the " + path + " directory!");
                throw new RuntimeException();
            }
        }
    }
}
