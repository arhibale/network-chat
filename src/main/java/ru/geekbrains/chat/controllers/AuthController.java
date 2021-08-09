package ru.geekbrains.chat.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

public class AuthController {
    private static final Logger LOG = LogManager.getLogger(AuthController.class.getName());

    @FXML
    private TextField loginTF;
    @FXML
    private PasswordField passwordTF;

    private DataInputStream in;
    private DataOutputStream out;

    @FXML
    private void initialize() throws IOException {
        openConnection();
        authentication();
    }

    private void authentication() {
        ExecutorService singleService = Executors.newSingleThreadExecutor();
        singleService.execute(() -> {
            try {
                while (true) {
                    LOG.info("Ожидание ответа от сервера...");
                    String strFromServer = in.readUTF();
                    if (strFromServer.startsWith("/authok")) {
                        Config.nick = strFromServer.split(" ")[1];
                        Platform.runLater(() -> {
                            Stage stage = (Stage) loginTF.getScene().getWindow();
                            stage.close();
                        });
                        LOG.info("Успешная авторизация...");
                        break;
                    } else if (strFromServer.startsWith("/warn")) {
                        String warn = strFromServer.split(":")[1];
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.WARNING);
                            alert.setTitle("Внимание!");
                            alert.setHeaderText("Введены неккоректные данные");
                            alert.setContentText(warn);
                            alert.show();
                            LOG.warn("Введены неккоректные данные: {}", warn);
                        });
                    }
                }
            } catch (EOFException | SocketException e) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Ошибка подключения");
                    alert.setHeaderText("Ошибка подключения!");
                    alert.setContentText("Вы были отключены");
                    alert.showAndWait();
                    Stage stage = (Stage) loginTF.getScene().getWindow();
                    stage.close();
                    LOG.fatal("Непредвиденная ошибка! {}", e.getMessage());
                    LOG.fatal(Arrays.toString(e.getStackTrace()));
                });
            } catch (Exception e) {
                e.printStackTrace();
                LOG.fatal("Непредвиденная ошибка! {}", e.getMessage());
                LOG.fatal(Arrays.toString(e.getStackTrace()));
            }
        });
        singleService.shutdown();
    }

    private void openConnection() throws IOException {
        Socket socket = ServerConnection.getSocket();
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        LOG.info("Открытие соединения с сервером...");
    }

    @FXML
    private void auth() throws IOException {
        String authString = "/auth " + loginTF.getText() + " " + passwordTF.getText();
        out.writeUTF(authString);
        LOG.info("Отправка серверу логина и пароля...");
    }
}

