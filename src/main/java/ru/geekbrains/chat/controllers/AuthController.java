package ru.geekbrains.chat.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.geekbrains.chat.config.Commands;
import ru.geekbrains.chat.config.Nick;
import ru.geekbrains.chat.service.ServerConnection;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
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
                    String strFromServer = in.readUTF();
                    if (strFromServer.startsWith("/" + Commands.AUTHOK)) {
                        Nick.nick = strFromServer.split(" ")[1];
                        Platform.runLater(() -> {
                            Stage stage = (Stage) loginTF.getScene().getWindow();
                            stage.close();
                        });
                        LOG.info("Успешная авторизация!");
                        break;
                    } else if (strFromServer.startsWith("/" + Commands.WARN)) {
                        String warn = strFromServer.split(":")[1];
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.WARNING);
                            alert.setTitle("Внимание!");
                            alert.setHeaderText("Введены неккоректные данные");
                            alert.setContentText(warn);
                            alert.show();
                        });
                        LOG.info("Проблемы при авторизации...");
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
                });
                closeConnection();
                LOG.warn("Отключение от сервера...");
            } catch (Exception e) {
                e.printStackTrace();
                closeConnection();
            }
        });
        singleService.shutdown();
    }

    private void openConnection() throws IOException {
        Socket socket = ServerConnection.getSocket();
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
    }

    private void closeConnection() {
        try {
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void auth() throws IOException {
        String authString = String.format("/%s %s %s", Commands.AUTH, loginTF.getText(), passwordTF.getText());
        LOG.info("Попытка авторизации...");
        System.out.println(authString);
        out.writeUTF(authString);
    }
}

