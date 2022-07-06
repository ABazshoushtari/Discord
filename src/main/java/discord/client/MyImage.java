package discord.client;

import javafx.scene.image.Image;

import java.io.Serializable;

public class MyImage implements Serializable {
    // Fields:
    private Image image;

    // Constructors:
    public MyImage(Image image) {
        this.image = image;
    }

    public MyImage(String path) {
        image = new Image(path);
    }

    // Methods:
    // Getters:
    public Image getImage() {
        return image;
    }

    // Setters:
    public void setImage(Image image) {
        this.image = image;
    }
}
