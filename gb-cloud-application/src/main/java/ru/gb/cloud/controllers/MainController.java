package ru.gb.cloud.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import ru.gb.cloud.network.Net;

import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
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

    private List<String> readLocalFiles() {
        String[] files = local_dir.list();
        if (files == null) {
            return List.of();
        }
            return Arrays.stream(files).toList();
    }

    private void read() {
        try {
            while (true) {
                String command = net.readUtf();
                if (command.equals("#list#")) {
                    readListFiles();
                }
                if (command.equals("#status#")) {
                    String status = net.getIs().readUTF();
                    input.setText(status);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            local_dir = new File("LocalFiles");
            net = new Net("localhost", 8189);
            Thread readThread = new Thread(this::read);
            readThread.setDaemon(true);
            readThread.start();
            clientView.getItems().addAll(readLocalFiles());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void selectFile(javafx.scene.input.MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            input.clear();
            final String selectedFile = clientView.getSelectionModel().getSelectedItem();
            input.setText(selectedFile);
            input.requestFocus();
            input.selectEnd();
        }
    }

    public void sendFile(String fileName) throws Exception{
      net.getOs().writeUTF("#addFile#");
      net.getOs().writeUTF(fileName);
      File file = local_dir.toPath().resolve(fileName).toFile();
      net.getOs().writeLong(file.length());
      byte[] buffer = new byte[256];
      try(InputStream fis = new FileInputStream(file)) {
          while (fis.available() > 0) {
             int readCount =  fis.read(buffer);
             net.getOs().write(buffer, 0, readCount);
          }
      }
      net.getOs().flush();
    }

    public void onClickSendButton(ActionEvent actionEvent) throws Exception {
        String file = input.getText();
        if (file != null && !file.isEmpty()) {
            sendFile(file);
            input.clear();
            input.requestFocus();
        }
    }
}
