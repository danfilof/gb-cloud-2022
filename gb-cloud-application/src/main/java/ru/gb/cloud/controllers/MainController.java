package ru.gb.cloud.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import lombok.extern.slf4j.Slf4j;
import ru.gb.cloud.model.*;
import ru.gb.cloud.network.Net;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
public class MainController implements Initializable {
    public ListView<String> clientView;
    public ListView<String> serverView;
    public Button cancelAuthButton;
    public ImageView failedAuthImage;
    public TextArea failedAuthMessage;
    @FXML
    public AnchorPane mainAnchorPane;
    @FXML
    public Button dropSelectionButton;
    @FXML
    public Button buttonBACK;
    public Button buttonBACKServer;
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

    private Path clientFileTreeDir;
    private Path serverFileTreeDir;

    private int countClientClick = 0;

    private int countServerClick = 0;
    private ArrayList<String> clientDirList = new ArrayList<>();
    private ArrayList<String> serverDirList = new ArrayList<>();
    private String dirs = null;

    private String serverDirs = null;

    private void read() {
        try {
            while (true) {
                AbstractMessage message = net.read();
                if (message instanceof ListMessage lm) {
                    System.out.println("received ServerList..." + lm.getFiles());
                    serverView.getItems().clear();
                    serverView.getItems().addAll(lm.getFiles());
                }

                if (message instanceof FileMessage file) {
                    System.out.println("received file to be downloaded: " + file.getName());
                    Files.write(clientDir.resolve(file.getName()), file.getBytes());
                    reloadList();
                }

                if (message instanceof AuthMessage authMessage) {
                    String status = authMessage.getAuthData();
                    log.info("received authentification status: " + status);
                    if (status.equals("%OK")) {
                        log.info("successfully authenticated...");
                        loginBox.setVisible(false);
                        failedAuthMessage.setVisible(false);
                        failedAuthImage.setVisible(false);
                        //mainAnchorPane.setStyle("-fx-background-color: linear-gradient(#4568DC, #B06AB3);");
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

    public void openClientDirectories(MouseEvent mouseEvent) throws IOException {
        if (mouseEvent.getClickCount() == 2) {
            buttonBACK.setVisible(true);
            //count how many times happens a double click
            countClientClick++;
            if (countClientClick == 1) {
                clientDirList.add(0,"LocalFiles");
            }
            String clientSelection = clientView.getSelectionModel().getSelectedItem();

            if (clientSelection.contains(".txt")) {
                System.out.println("Selected is a .txt file");
                System.exit(0);
            }
            // after selecting the folder to be opened the name of the folder is added into the arraylist
            clientDirList.add(clientSelection);
            System.out.println("BEFORE (!) : (!) dirList: " + clientDirList);
            // reads the arraylist into the array
            String[] clientDirsArray = Arrays.copyOf(clientDirList.toArray(), clientDirList.size(), String[].class);
            // reads the array into string
            dirs = Arrays.toString(clientDirsArray);
            System.out.println("dirs: " + dirs);
            // getting rid of array syntax which isn't necessary
            dirs = dirs.replace(", ", "/");
            dirs = dirs.replace("[", "");
            dirs = dirs.replace("]", "");
            System.out.println("dirs: " + dirs);
            // using the string as a path
            clientFileTreeDir = Path.of(dirs);
            System.out.println("localDir: " + clientFileTreeDir);
            clientView.getItems().clear();
            clientView.getItems().addAll(Files.list(clientFileTreeDir).map(Path::getFileName).map(Path::toString).toList());
            System.out.println("---------------------");
        }
    }

    public void buttonBACK(ActionEvent actionEvent) throws IOException {
        int listSizeLastValue = clientDirList.size() - 1;
        clientDirList.remove(listSizeLastValue);
        String[] dirsArray = Arrays.copyOf(clientDirList.toArray(), clientDirList.size(), String[].class);
        dirs = Arrays.toString(dirsArray);
        dirs = dirs.replace(", ", "/");
        dirs = dirs.replace("[", "");
        dirs = dirs.replace("]", "");
        clientFileTreeDir = Path.of(dirs);
        System.out.println("return to: " + clientFileTreeDir);
        clientView.getItems().clear();
        clientView.getItems().addAll(Files.list(clientFileTreeDir).map(Path::getFileName).map(Path::toString).toList());

        if (clientFileTreeDir == Path.of("LocalFiles") || dirs.equals("LocalFiles")) {
            buttonBACK.setVisible(false);
        }
    }
    public void openServerDirectories(MouseEvent mouseEvent) throws IOException {
        if (mouseEvent.getClickCount() == 2) {
            buttonBACKServer.setVisible(true);
            countServerClick++;
            if (countServerClick == 1) {
                serverDirList.add(0, "ServerFiles");
            }
            String serverSelection = serverView.getSelectionModel().getSelectedItem();
            System.out.println("selected file on server: " + serverSelection);
            if (serverSelection.contains(".txt")) {
                System.out.println("Selected is a .txt file");
                System.exit(0);
            }
            serverDirList.add(serverSelection);
            String[] serverDirsArray = Arrays.copyOf(serverDirList.toArray(), serverDirList.size(), String[].class);
            serverDirs = Arrays.toString(serverDirsArray);
            System.out.println("dirs on server: " + serverDirs);
            serverDirs = serverDirs.replace(", ", "/");
            serverDirs = serverDirs.replace("[", "");
            serverDirs = serverDirs.replace("]", "");
            serverFileTreeDir = Path.of(serverDirs);
            System.out.println(serverFileTreeDir);
            String serverTree = String.valueOf(serverFileTreeDir);
            net.write(new directoryMessage(serverTree));
        }
    }

    public void buttonBACKServer(ActionEvent actionEvent) throws IOException {
        int serverListSizeLastValue = serverDirList.size() - 1;
        serverDirList.remove(serverListSizeLastValue);
        String[] serverDirsArray = Arrays.copyOf(serverDirList.toArray(), serverDirList.size(), String[].class);
        serverDirs = Arrays.toString(serverDirsArray);
        serverDirs = serverDirs.replace(", ", "/");
        serverDirs = serverDirs.replace("[", "");
        serverDirs = serverDirs.replace("]", "");
        serverFileTreeDir = Path.of(serverDirs);
        System.out.println("return to: " + serverFileTreeDir);
        String serverTreeReturn = String.valueOf(serverFileTreeDir);
        net.write(new directoryMessage(serverTreeReturn));

        if (serverFileTreeDir == Path.of("ServerFiles") || serverDirs.equals("ServerFiles")) {
            buttonBACKServer.setVisible(false);
        }
    }

    public void upload(ActionEvent actionEvent) throws Exception {
        String fileName = clientView.getSelectionModel().getSelectedItem();
        log.info("sent file: " + fileName);
        net.write(new FileMessage(clientFileTreeDir.resolve(fileName)));
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
        String fileToDeleteOnServer = serverView.getSelectionModel().getSelectedItem();
        System.out.println("sent request to delete a file: " + fileToDeleteOnServer);
        // send the name of the file that should be deleted (string)
        net.write(new DeleteMessage(fileToDeleteOnServer));

        String fileToDeleteLocal = clientView.getSelectionModel().getSelectedItem();
        Path toDelete = Path.of("LocalFiles", fileToDeleteLocal);
        Files.deleteIfExists(toDelete);
        reloadList();

        serverView.getSelectionModel().clearSelection();
        clientView.getSelectionModel().clearSelection();
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
        String serverFileToRename = serverView.getSelectionModel().getSelectedItem();
        String localFileToRename = clientView.getSelectionModel().getSelectedItem();
        String newFileName = newFileNameField.getText();

        if (localFileToRename == null) {
            String authData = serverFileToRename + "#" + newFileName;
            net.write(new ChangeFileNameMessage(authData));
            fileNameChangeBox.setVisible(false);
            newFileNameField.clear();
        }

        if (serverFileToRename == null) {
            File originalFile = new File("LocalFiles", localFileToRename);
            File newFile = new File("LocalFiles", newFileName);

            if (localFileToRename != null ) {
                if (newFile.exists()) {
                    System.out.println("file exists already");
                }
                boolean successFileNameChange = originalFile.renameTo(newFile);

                if (!successFileNameChange) {
                    System.out.println("Something went wrong, cannot rename a file");
                }
                fileNameChangeBox.setVisible(false);
                newFileNameField.clear();
                reloadList();
            } else {
                System.out.println("no data given");
            }
        }
    }

    public void dropSelectionOnClientList(MouseEvent mouseEvent) {
        clientView.getSelectionModel().clearSelection();
    }

    public void dropSelectionOnServerList(MouseEvent mouseEvent) {
        serverView.getSelectionModel().clearSelection();
    }
}
