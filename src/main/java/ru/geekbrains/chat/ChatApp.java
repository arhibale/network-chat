package ru.geekbrains.chat;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

public class ChatApp extends Application {
    private static final Logger LOG = LogManager.getLogger(ChatApp.class.getName());

    public static Stage mainStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        mainStage = primaryStage;
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/app/chat.fxml")));
        primaryStage.setTitle("Message (" + Config.nick + ")");
        primaryStage.setScene(new Scene(root, 500, 400));
        primaryStage.setResizable(false);
        LOG.info("Открытие окна чата...");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
