package ru.geekbrains.chat;

import javafx.scene.control.TextArea;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.ArrayList;

public class ChatHistory {
    private static final Logger LOG = LogManager.getLogger(ChatHistory.class.getName());
    private final String fileName = "history_[" + Config.nick + "].txt";
    private final File file;

    public ChatHistory() throws IOException {
        file = new File(fileName);
        if (!file.exists()) {
            if (file.createNewFile()) {
                LOG.info("Нет файла истории! Создание нового файла истории {}", fileName);
            } else {
                LOG.info("Загрузка файла истории {}", fileName);
            }
        }
    }

    public void openChatHistory(TextArea chatArea) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        ArrayList<String> str = new ArrayList<>(100);
        bufferedReader.lines().forEach(str::add);
        if (str.size() > 100) {
            for (int i = str.size() - 100; i < str.size(); i++) {
                chatArea.appendText(str.get(i) + "\n");
            }
        } else {
            for (String s : str) {
                chatArea.appendText(s + "\n");
            }
        }
        bufferedReader.close();
        LOG.info("Запись истории чата на главное окно...");
    }

    public void recordChatHistory(String strFromServer) throws IOException {
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file, true));
        bufferedWriter.write(strFromServer + "\n");
        bufferedWriter.close();
        LOG.info("Запись сообщения [{}] в файл {}", strFromServer, fileName);
    }
}
