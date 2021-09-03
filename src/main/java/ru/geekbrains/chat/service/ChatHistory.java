package ru.geekbrains.chat.service;

import javafx.scene.control.TextArea;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.geekbrains.chat.config.Nick;

import java.io.*;
import java.util.ArrayList;

public class ChatHistory {

    private static final Logger LOG = LogManager.getLogger(ChatHistory.class.getName());
    private File file;

    public ChatHistory() throws IOException {
        init();
    }

    private void init() throws IOException {
        String fileName = "history_[" + Nick.nick + "].txt";
        file = new File(fileName);
        if (!file.exists()) {
            if (file.createNewFile()) {
                LOG.info("Создание истории чата...");
            }
        }
    }

    public void openChatHistory(TextArea chatArea) throws IOException {
        LOG.info("Открытие истории чата...");

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
    }

    public void recordChatHistory(String strFromServer) throws IOException {
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file, true));
        bufferedWriter.write(strFromServer + "\n");
        bufferedWriter.close();
    }
}
