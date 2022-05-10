package ru.gb.cloud.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import lombok.extern.slf4j.Slf4j;
import ru.gb.cloud.model.*;
import ru.gb.cloud.network.Net;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ResourceBundle;

@Slf4j
public class MainController implements Initializable {
    public ListView<String> clientView;
    public ListView<String> serverView;
    public Button cancelAuthButton;
    public ImageView failedAuthImage;
    public TextArea failedAuthMessage;
    @FXML
    private TextField newFileNameField;
    @FXML
    private HBox fileNameChangeBox;

    private Net net;
   private Path clientDir;

    @FXML
    private Button confirmFileNameChangeButton;
   @FXML
   private Button deleteButton;

   @FXML
   private Button downloadButton;

   @FXML
   private Button renameButton;

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
                    log.info("received ServerList...");
                    serverView.getItems().clear();
                    serverView.getItems().addAll(lm.getFiles());
                }

                if (message instanceof FileMessage file) {
                    log.info("received file to be downloaded: " + file.getName());
                    Files.write(clientDir.resolve(file.getName()), file.getBytes());
                    reloadList();
                }

                if (message instanceof AuthMessage authMessage) {
                    String status = authMessage.getAuthData();
                    log.info("received authentification status: " + status);
                    if (status.equals("%OK")) {
                        log.info("successfully authenticated...");
                        loginBox.setVisible(false);
                        clientView.setVisible(true);
                        serverView.setVisible(true);
                        deleteButton.setVisible(true);
                        uploadButton.setVisible(true);
                        downloadButton.setVisible(true);
                        renameButton.setVisible(true);
                    } else {
                        log.info("user used wrong password or login...");
                        System.out.println("Wrong login or password");
                        InputStream stream = new FileInputStream("C:\\Java\\gb-cloud\\AuthPicture\\sad_robot.jpg");
                        Image image = new Image(stream);
                        failedAuthImage.setImage(image);
                        failedAuthImage.setVisible(true);
                        failedAuthMessage.setVisible(true);
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
        log.info("sent file: " + fileName);
        net.write(new FileMessage(clientDir.resolve(fileName)));
    }

    public void download(ActionEvent actionEvent) throws Exception {
        String downloadFile = serverView.getSelectionModel().getSelectedItem();
        log.info("sent request to download a file: " + downloadFile);
        // send the name of the file that should be downloaded (string)
        net.write(new DownloadMessage(downloadFile));
    }

    public void reloadList() throws IOException {
        clientView.getItems().clear();
        clientView.getItems().addAll(getClientFiles());
    }

    public void delete(ActionEvent actionEvent) throws IOException {
        String fileToDelete = serverView.getSelectionModel().getSelectedItem();
        log.info("sent request to delete a file: " + fileToDelete);
        // send the name of the file that should be deleted (string)
        net.write(new DeleteMessage(fileToDelete));
    }

    public void btnAuthClick(ActionEvent actionEvent) throws IOException {
        String login = loginField.getText();
        String password = passwordField.getText();
        String authData = login + "#" + password;
        // send "coded" authenticate data
        net.write(new AuthMessage(authData));
        log.info("sent request to authenticate...");
    }

    public void cancelAuthClick(ActionEvent actionEvent) {
        log.info("user closed the program by pressing cancel button...");
        System.exit(0);
    }

    public void rename(ActionEvent actionEvent) {
        fileNameChangeBox.setVisible(true);
    }

    public void confirmFileNameChange(ActionEvent actionEvent) throws IOException {
        String fileToRename = serverView.getSelectionModel().getSelectedItem();
        String newFileName = newFileNameField.getText();
        System.out.println(fileToRename + " ||| " + newFileName);
        String authData = fileToRename + "#" + newFileName;
        net.write(new ChangeFileNameMessage(authData));
        fileNameChangeBox.setVisible(false);

    }
}
