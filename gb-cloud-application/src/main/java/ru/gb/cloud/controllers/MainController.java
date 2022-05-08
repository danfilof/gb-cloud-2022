package ru.gb.cloud.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import lombok.extern.slf4j.Slf4j;
import ru.gb.cloud.model.*;
import ru.gb.cloud.network.Net;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ResourceBundle;

@Slf4j
public class MainController implements Initializable {
    public ListView<String> clientView;
    public ListView<String> serverView;
    private Net net;
   private Path clientDir;

   @FXML
   private Button deleteButton;

   @FXML
   private Button downloadButton;

   @FXML
   private Button uploadButton;

   @FXML
   private Button AuthButton;

   @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;

    @FXML HBox loginBox;

    private void read() {
        try {
            while (true) {
                AbstractMessage message = net.read();

                if (message instanceof ListMessage lm) {
                    serverView.getItems().clear();
                    serverView.getItems().addAll(lm.getFiles());
                }

                if (message instanceof FileMessage file) {
                    Files.write(clientDir.resolve(file.getName()), file.getBytes());
                    reloadList();
                }

                if (message instanceof AuthMessage authMessage) {
                    String status = authMessage.getAuthData();
                    if (status.equals("%OK")) {
                        loginBox.setVisible(false);
                        clientView.setVisible(true);
                        serverView.setVisible(true);
                        deleteButton.setVisible(true);
                        uploadButton.setVisible(true);
                        downloadButton.setVisible(true);
                    }

                    if (message.equals("%WrongData")) {
                        System.out.println("Wrong login or password");
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private List<String> getClientFiles() throws IOException {
        return Files.list(clientDir).map(Path::getFileName).map(Path::toString).toList();
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            clientDir = Path.of("LocalFiles");
            clientView.getItems().clear();
            clientView.getItems().addAll(getClientFiles());
            net = new Net("localhost", 8189);
            Thread.sleep(300);
            Thread readThread = new Thread(this::read);
            readThread.setDaemon(true);
            readThread.start();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void upload(ActionEvent actionEvent) throws Exception {
        String fileName = clientView.getSelectionModel().getSelectedItem();
        net.write(new FileMessage(clientDir.resolve(fileName)));
    }

    public void download(ActionEvent actionEvent) throws Exception {
        String downloadFile = serverView.getSelectionModel().getSelectedItem();
        net.write(new DownloadMessage(downloadFile));
    }

    public void reloadList() throws IOException {
        clientView.getItems().clear();
        clientView.getItems().addAll(getClientFiles());
    }

    public void delete(ActionEvent actionEvent) throws IOException {
        String fileToDelete = serverView.getSelectionModel().getSelectedItem();
        net.write(new DeleteMessage(fileToDelete));
    }

    public void btnAuthClick(ActionEvent actionEvent) throws IOException {
        String login = loginField.getText();
        String password = passwordField.getText();
        String authData = login + "#" + password;
        System.out.println(authData);
        net.write(new AuthMessage(authData));
    }
}
