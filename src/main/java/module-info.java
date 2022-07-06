module discord.advancedprogrammingfinalproject {
    requires javafx.controls;
    requires javafx.fxml;
    requires charm.glisten;
    requires java.desktop;


    opens discord.client to javafx.fxml;
    exports discord.client;
}