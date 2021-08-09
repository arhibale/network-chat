package ru.geekbrains.chat.controllers;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.geekbrains.chat.ChatApp;
import ru.geekbrains.chat.ChatHistory;
import ru.geekbrains.chat.Config;
import ru.geekbrains.chat.ServerConnection;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatController {
    private static final Logger LOG = LogManager.getLogger(ChatController.class.getName());
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    @FXML
    private ListView<String> users;
    @FXML
    private TextArea mainTextArea;
    @FXML
    private TextField textField;

    @FXML
    public void initialize() {
        try {
            openLoginWindow();
            openConnection();
            readingServer();
            addCloseListener();
        } catch (Exception e) {
            closeWindow();
            openErrorWindow();
            e.printStackTrace();
        }
    }

    private void openLoginWindow() throws IOException {
        Parent root = FXMLLoader.load(ClassLoader.getSystemResource("app/auth.fxml"));
        Stage loginStage = new Stage();
        loginStage.initModality(Modality.APPLICATION_MODAL);
        loginStage.setScene(new Scene(root, 300, 140));
        loginStage.setResizable(false);
        loginStage.setTitle("Вход");
        LOG.info("Открытие окна авторизации...");
        loginStage.showAndWait();
    }

    private void readingServer() {
        ExecutorService singleService = Executors.newSingleThreadExecutor();
        singleService.execute(() -> {
            try {
                ChatHistory historyController = new ChatHistory();
                historyController.openChatHistory(mainTextArea);
                while (socket.isConnected()) {
                    LOG.info("Ожидание ответа сервера...");
                    String strFromServer = in.readUTF();
                    historyController.recordChatHistory(strFromServer);
                    LOG.info("Ответ сервера: {}", strFromServer);
                    if (strFromServer.startsWith("/end")) {
                        return;
                    } else if (strFromServer.startsWith("/nn")) {
                        setTitleForNick(strFromServer);
                        continue;
                    } else if (strFromServer.startsWith("/clients")) {
                        clientsList(strFromServer);
                        continue;
                    }
                    printMessage(strFromServer);
                }
            } catch (EOFException | SocketException e) {
                closeWindow();
                LOG.fatal("Непредвиденная ошибка! {}", e.getMessage());
                LOG.fatal(Arrays.toString(e.getStackTrace()));
            } catch (IOException e) {
                e.printStackTrace();
                LOG.fatal("Непредвиденная ошибка! {}", e.getMessage());
                LOG.fatal(Arrays.toString(e.getStackTrace()));
            } finally {
                closeConnection();
            }
        });
        singleService.shutdown();
    }

    private void setTitleForNick(String str) {
        Platform.runLater(() -> {
            Config.nick = str.split(" ")[1];
            ChatApp.mainStage.setTitle("Message (" + Config.nick + ")");
            LOG.info("Смена никнейма...");
        });
    }

    private void openConnection() throws IOException {
        socket = ServerConnection.getSocket();
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        LOG.info("Открытие соединения с сервером...");
    }

    private void addCloseListener() {
        EventHandler<WindowEvent> onCloseRequest = ChatApp.mainStage.getOnCloseRequest();
        ChatApp.mainStage.setOnCloseRequest(event -> {
            closeConnection();
            if (onCloseRequest != null) {
                onCloseRequest.handle(event);
            }
        });
    }

    private void closeConnection() {
        try {
            socket.close();
            out.close();
            in.close();
            LOG.info("Закрытие подключения с сервером...");
        } catch (IOException e) {
            e.printStackTrace();
            LOG.fatal("Непредвиденная ошибка! {}", e.getMessage());
            LOG.fatal(Arrays.toString(e.getStackTrace()));
        }
    }

    @FXML
    private void sendMessage() {
        if (!textField.getText().trim().isEmpty()) {
            try {
                out.writeUTF(textField.getText().trim());
                LOG.info("Отправка сообщения на сервер: {}", textField.getText().trim());
                textField.clear();
                textField.requestFocus();
            } catch (SocketException e) {
                openErrorWindow();
                closeWindow();
                e.printStackTrace();
                LOG.fatal("Непредвиденная ошибка! {}", e.getMessage());
                LOG.fatal(Arrays.toString(e.getStackTrace()));
            } catch (IOException e) {
                e.printStackTrace();
                LOG.fatal("Непредвиденная ошибка! {}", e.getMessage());
                LOG.fatal(Arrays.toString(e.getStackTrace()));
            }
        }
    }

    private void printMessage(String str) {
        if (!str.trim().isEmpty()) {
            mainTextArea.appendText(str.trim() + "\n");
            LOG.info("Печать сообщения на главное окно: {}", str);
        }
    }

    private void closeWindow() {
        Platform.runLater(() -> {
            Stage stage = (Stage) mainTextArea.getScene().getWindow();
            stage.close();
            LOG.warn("Закрытие окна...");
        });
    }

    private void openErrorWindow() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка подключения");
            alert.setHeaderText("Ошибка подключения!");
            alert.setContentText("Разрыв соединения");
            LOG.warn("Открытие окна ошибки...");
            alert.showAndWait();
        });
    }

    private void clientsList(String strFromServer) {
        Platform.runLater(() -> {
            String str = strFromServer.split("/clients")[1];
            String[] str2 = str.split(":");
            users.getItems().clear();
            users.getItems().addAll(str2);
            LOG.info("Обновление списка пользователей...");
        });
    }
}
