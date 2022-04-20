package ru.gb.cloud.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import ru.gb.cloud.network.Net;

import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    public Button sendButton;
    private Net net;
    public ListView<String> view;
    public ListView<String> clientView;
    public TextField input;
    private File local_dir;

    private void readListFiles() {
        try {
            view.getItems().clear();
            Long filesCount = net.readLong();
            for (int i = 0; i < filesCount; i++) {
                String fileName = net.readUtf();
                view.getItems().addAll(fileName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readLocalFiles() {
        try {
            clientView.getItems().clear();
            local_dir = new File("LocalFiles");
            String[] files = local_dir.list();
            clientView.getItems().addAll(files);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void read() {
        try {
            while (true) {
                String command = net.readUtf();
                if (command.equals("#list#")) {
                    readListFiles();
                    readLocalFiles();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            net = new Net("localhost", 8189);
            Thread readThread = new Thread(this::read);
            readThread.setDaemon(true);
            readThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void selectFile(javafx.scene.input.MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            final String selectedFile = clientView.getSelectionModel().getSelectedItem();
            input.setText(selectedFile);
            input.requestFocus();
            input.selectEnd();
        }
    }

    public void sendFile(String file) {
        try {
            net.getOs().writeUTF("#addFile#");
            net.getOs().writeUTF(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onClickSendButton(ActionEvent actionEvent) {
        String file = input.getText();
        if (file != null && !file.isEmpty()) {
            sendFile(file);
            input.clear();
            input.requestFocus();
        }
    }
}
