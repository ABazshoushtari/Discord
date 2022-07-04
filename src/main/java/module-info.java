module discord.advancedprogrammingfinalproject {
    requires javafx.controls;
    requires javafx.fxml;


    opens discord.client to javafx.fxml;
    exports discord.client;
}