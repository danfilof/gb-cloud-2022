package ru.gb.cloud.controllers;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import ru.gb.cloud.model.*;
import ru.gb.cloud.network.Net;

import java.io.*;
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
    public TextField newFolderNameField;
    @FXML
    public ListView contextMenu;
    @FXML
    public VBox buttonSelectionBox;
    @FXML
    public ContextMenu localCM;
    @FXML
    public ContextMenu serverCM;
    @FXML
    public Button confirmNewFolderButton;
    @FXML
    public StackPane changeFileNamePane;
    @FXML
    public MenuItem MIUpload;
    @FXML
    public MenuItem MIRename;
    @FXML
    public MenuItem MINewFolder;
    @FXML
    public MenuItem MIMove;
    @FXML
    public MenuItem MIDelete;
    @FXML
    public MenuItem MIDOWNLOAD;
    @FXML
    public MenuItem MIRENAME;
    @FXML
    public MenuItem MICREATEFOLDER;
    @FXML
    public MenuItem MIMOVE;
    @FXML
    public MenuItem MIDELETE;
    @FXML
    public TextField clientTextArea;
    @FXML
    public TextField serverTextArea;
    @FXML
    public TextField welcomeField;
    @FXML
    private Button confirmMoveButton;
    @FXML
    private Button moveButton;
    @FXML
    private Button buttonBACK;
    @FXML
    private Button buttonBACKServer;
    @FXML
    private Button createNewFolderButton;
    @FXML
    private StackPane createNewFolderPane;
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

    private String initialLocalDir = null;
    private String initialServerDir = null;
    private String clientSelectedFileToMove = null;
    private String ServerSelectedFileToMove = null;

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
        if (mouseEvent.getClickCount() == 2 && clientView.getSelectionModel().getSelectedItem() != null) {
            buttonBACK.setVisible(true);
            //count how many times does the double click happen
            countClientClick++;
            if (countClientClick == 1) {
                clientDirList.add(0,"LocalFiles");
            }
            String clientSelection = clientView.getSelectionModel().getSelectedItem();
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

            // TODO if selected is a file
            if (clientSelection.contains(".txt")) {
                System.out.println("Selected is a .txt file");
                try
                {
                    byte[] bytes = Files.readAllBytes(Paths.get(String.valueOf(clientFileTreeDir)));
                    String fileContent = new String (bytes);
                    System.out.println("fileContent: " + fileContent);
                    clientView.getItems().clear();
                    clientView.getItems().add(fileContent);
                    clientView.getItems().addAll(Files.list(clientFileTreeDir).map(Path::getFileName).map(Path::toString).toList());
                    System.out.println("---------------------");
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            } else {
                clientView.getItems().clear();
                clientView.getItems().addAll(Files.list(clientFileTreeDir).map(Path::getFileName).map(Path::toString).toList());
                System.out.println("---------------------");
            }

        }
    }

    public void buttonBACK(ActionEvent actionEvent) throws IOException {
        //this button allows to return to previous directory
        int listSizeLastValue = clientDirList.size() - 1;
        clientDirList.remove(listSizeLastValue);
        String[] dirsArray = Arrays.copyOf(clientDirList.toArray(), clientDirList.size(), String[].class);
        dirs = Arrays.toString(dirsArray);
        // getting rid of array syntax which isn't necessary
        dirs = dirs.replace(", ", "/");
        dirs = dirs.replace("[", "");
        dirs = dirs.replace("]", "");
        clientFileTreeDir = Path.of(dirs);
        System.out.println("return to: " + clientFileTreeDir);
        clientView.getItems().clear();
        clientView.getItems().addAll(Files.list(clientFileTreeDir).map(Path::getFileName).map(Path::toString).toList());

        if (clientFileTreeDir == Path.of("LocalFiles") || dirs.equals("LocalFiles")) {
            // if the user reaches the "root folder" the button disappears in order to avoid user going out of the folder
            buttonBACK.setVisible(false);
        }
    }
    public void openServerDirectories(MouseEvent mouseEvent) throws IOException {
        if (mouseEvent.getClickCount() == 2 && serverView.getSelectionModel().getSelectedItem() != null) {
            buttonBACKServer.setVisible(true);
            countServerClick++;
            if (countServerClick == 1) {
                // every time user any new directory the [0] index in array list becomes "root folder"
                serverDirList.add(0, "ServerFiles");
            }
            String serverSelection = serverView.getSelectionModel().getSelectedItem();
            System.out.println("selected file on server: " + serverSelection);

            if (serverSelection.contains(".txt")) {
                System.out.println("Selected is a .txt file");
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
            System.out.println("serverTree: " + serverTree);
            // sending the path needed to the server
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

    // In every futher usage "serverFileTreeDir" and "clientFileTreeDir" are basically the directories which are opened
    // in the ListViews.

    // if the serverFileTreeDir or clientFileTreeDir == null, that means that the file is in the 'root folder' - either "LocalFiles" or
    // "ServerFiles" for all other further usages

    // for any further usage, there is a script that drops selection on a first list when a mouse enters the second
    private void read() {
        try {
            while (true) {
                AbstractMessage message = net.read();
                if (message instanceof ListMessage lm) {
                    // In order to avoid jfx exception, use Platform.runLater
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println("received ServerList..." + lm.getFiles());
                            serverView.getItems().clear();
                            serverView.getItems().addAll(lm.getFiles());
                        }
                    });

                }

                if (message instanceof fileContentMessage fileContentMessage) {
                    String serverFileContent = fileContentMessage.getFileText();

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            serverView.getItems().clear();
                            serverView.getItems().add(serverFileContent);
                        }
                    });
                }

                if (message instanceof FileMessage file) {
                    System.out.println("received file to be downloaded: " + file.getName());
                    Files.write(clientDir.resolve(file.getName()), file.getBytes());
                    reloadList();
                }

                if (message instanceof AuthMessage authMessage) {
                    String authData = authMessage.getAuthData();
                    System.out.println("AuthData: " + authData);
                    log.info("received authentification status: " + authData);
                    String[] authDATA = authData.split("#");
                    String status = authDATA[1];
                    String nick = authDATA[0];

                    if (status.equals("OK")) {
                        log.info("successfully authenticated...");
                        // make all buttons and list visible
                        loginBox.setVisible(false);
                        failedAuthMessage.setVisible(false);
                        failedAuthImage.setVisible(false);
                        clientView.setVisible(true);
                        serverView.setVisible(true);
                        clientTextArea.setVisible(true);
                        serverTextArea.setVisible(true);
                        welcomeField.setVisible(true);
                        welcomeField.setText("Welcome back, " + nick + " !");

                        PauseTransition visiblePause = new PauseTransition(
                                Duration.seconds(3)
                        );
                        visiblePause.setOnFinished(
                                event -> welcomeField.setVisible(false)
                        );
                        visiblePause.play();
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                // add some background gradient
                                mainAnchorPane.setStyle("-fx-background-color: linear-gradient(#328BDB 0%, #207BCF 25%, #1973C9 75%, #0A65BF 100%);");

                                ImageView deleteImage = new ImageView();
                                InputStream deleteStream = null;

                                ImageView deleteImageII = new ImageView();
                                InputStream deleteStreamII = null;

                                ImageView uploadImage = new ImageView();
                                InputStream uploadStream = null;

                                ImageView downloadImage = new ImageView();
                                InputStream downloadStream = null;

                                ImageView renameImage = new ImageView();
                                InputStream renameStream = null;

                                ImageView renameImageII = new ImageView();
                                InputStream renameStreamII = null;

                                ImageView newFolderImage = new ImageView();
                                InputStream newFolderStream = null;

                                ImageView newFolderImageII = new ImageView();
                                InputStream newFolderStreamII = null;

                                ImageView moveImage = new ImageView();
                                InputStream moveStream = null;

                                ImageView moveImageII = new ImageView();
                                InputStream moveStreamII = null;
//
                                ImageView returnImage = new ImageView();
                                InputStream returnStream = null;

                                ImageView returnImageII = new ImageView();
                                InputStream returnStreamII = null;
                                try {
                                    deleteStream = new FileInputStream("C:\\Java\\gb-cloud\\AuthPicture\\delete.jpg");
                                    uploadStream = new FileInputStream("C:\\Java\\gb-cloud\\AuthPicture\\upload.png");
                                    downloadStream = new FileInputStream("C:\\Java\\gb-cloud\\AuthPicture\\download.png");
                                    renameStream = new FileInputStream("C:\\Java\\gb-cloud\\AuthPicture\\rename.png");
                                    renameStreamII = new FileInputStream("C:\\Java\\gb-cloud\\AuthPicture\\rename.png");
                                    newFolderStream = new FileInputStream("C:\\Java\\gb-cloud\\AuthPicture\\createNewFolder.png");
                                    newFolderStreamII = new FileInputStream("C:\\Java\\gb-cloud\\AuthPicture\\createNewFolder.png");
                                    moveStream = new FileInputStream("C:\\Java\\gb-cloud\\AuthPicture\\move.png");
                                    moveStreamII = new FileInputStream("C:\\Java\\gb-cloud\\AuthPicture\\move.png");
                                    returnStream = new FileInputStream("C:\\Java\\gb-cloud\\AuthPicture\\return.png");
                                    returnStreamII = new FileInputStream("C:\\Java\\gb-cloud\\AuthPicture\\return.png");
                                    deleteStreamII = new FileInputStream("C:\\Java\\gb-cloud\\AuthPicture\\delete.jpg");
                                } catch (FileNotFoundException e) {
                                    throw new RuntimeException(e);
                                }
                                Image deleteI = new Image(deleteStream);
                                deleteImage.setImage(deleteI);
                                deleteImage.setFitWidth(20);
                                deleteImage.setFitHeight(20);
                                MIDelete.setGraphic(deleteImage);

                                Image deleteII = new Image(deleteStreamII);
                                deleteImageII.setImage(deleteII);
                                deleteImageII.setFitWidth(20);
                                deleteImageII.setFitHeight(20);
                                MIDELETE.setGraphic(deleteImageII);
//
                                Image uploadI = new Image(uploadStream);
                                uploadImage.setImage(uploadI);
                                uploadImage.setFitWidth(20);
                                uploadImage.setFitHeight(20);
                                MIUpload.setGraphic(uploadImage);

                                Image downloadI = new Image(downloadStream);
                                downloadImage.setImage(downloadI);
                                downloadImage.setFitWidth(20);
                                downloadImage.setFitHeight(20);
                                MIDOWNLOAD.setGraphic(downloadImage);

                                Image renameI = new Image(renameStream);
                                renameImage.setImage(renameI);
                                renameImage.setFitWidth(20);
                                renameImage.setFitHeight(20);
                                MIRename.setGraphic(renameImage);

                                Image renameII = new Image(renameStreamII);
                                renameImageII.setImage(renameII);
                                renameImageII.setFitWidth(20);
                                renameImageII.setFitHeight(20);
                                MIRENAME.setGraphic(renameImageII);

                                Image newFolderI = new Image(newFolderStream);
                                newFolderImage.setImage(newFolderI);
                                newFolderImage.setFitWidth(20);
                                newFolderImage.setFitHeight(20);
                                MINewFolder.setGraphic(newFolderImage);

                                Image newFolderII = new Image(newFolderStreamII);
                                newFolderImageII.setImage(newFolderII);
                                newFolderImageII.setFitWidth(20);
                                newFolderImageII.setFitHeight(20);
                                MICREATEFOLDER.setGraphic(newFolderImageII);

                                Image moveI = new Image(moveStream);
                                moveImage.setImage(moveI);
                                moveImage.setFitWidth(20);
                                moveImage.setFitHeight(20);
                                MIMove.setGraphic(moveImage);

                                Image moveII = new Image(moveStreamII);
                                moveImageII.setImage(moveII);
                                moveImageII.setFitWidth(20);
                                moveImageII.setFitHeight(20);
                                MIMOVE.setGraphic(moveImageII);

                                Image returnI = new Image(returnStream);
                                returnImage.setImage(returnI);
                                returnImage.setFitWidth(15);
                                returnImage.setFitHeight(15);
                                buttonBACK.setGraphic(returnImage);

                                Image returnII = new Image(returnStreamII);
                                returnImageII.setImage(returnII);
                                returnImageII.setFitWidth(15);
                                returnImageII.setFitHeight(15);
                                buttonBACKServer.setGraphic(returnImageII);
                            }
                        });
                    } else {
                        log.info("user used wrong password or login...");
                        System.out.println("Wrong login or password");
                        // upload image
                        InputStream streamRobot = new FileInputStream("C:\\Java\\gb-cloud\\AuthPicture\\sad_robot.jpg");
                        Image robotImage = new Image(streamRobot);
                        failedAuthImage.setImage(robotImage);
                        failedAuthImage.setVisible(true);
                        failedAuthMessage.setVisible(true);
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void upload(ActionEvent actionEvent) throws Exception {
        String fileName = clientView.getSelectionModel().getSelectedItem();
        log.info("sent file: " + fileName);
        if (clientFileTreeDir == null) {
            net.write(new FileMessage(clientDir.resolve(fileName)));
        } else {
            net.write(new FileMessage(clientFileTreeDir.resolve(fileName)));
        }
    }

    public void download(ActionEvent actionEvent) throws Exception {
        String selectedFileToDownload = serverView.getSelectionModel().getSelectedItem();
        log.info("sent request to download a file: " + selectedFileToDownload);
        String downloadFileDir = serverFileTreeDir + "%" + selectedFileToDownload;
        // send the name of the file that should be downloaded (string)
        if (serverFileTreeDir == null) {
            net.write(new DownloadMessage("null" + "%" + selectedFileToDownload));
        } else {
            net.write(new DownloadMessage(downloadFileDir));
        }
    }

    public void reloadList() throws IOException {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                clientView.getItems().clear();
                try {
                    clientView.getItems().addAll(getClientFiles());

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public void delete(ActionEvent actionEvent) throws IOException {
        String fileToDeleteOnServer = serverView.getSelectionModel().getSelectedItem();
        System.out.println("sent request to delete a file: " + fileToDeleteOnServer);
        String deleteFileDir = serverFileTreeDir + "%" + fileToDeleteOnServer;
        if (serverFileTreeDir == null) {
            net.write(new DeleteMessage("null" + "%" + fileToDeleteOnServer));
            serverCM.hide();
        } else {
            net.write(new DeleteMessage(deleteFileDir));
            serverCM.hide();
        }

        String fileToDeleteLocal = clientView.getSelectionModel().getSelectedItem();

        if (clientFileTreeDir == null) {
            Path toDelete = Path.of("LocalFiles", fileToDeleteLocal);
            Files.deleteIfExists(toDelete);
            reloadList();
            serverCM.hide();
        } else {
            Path toBeDeleted = Path.of(String.valueOf(clientFileTreeDir), fileToDeleteLocal);
            Files.deleteIfExists(toBeDeleted);
            clientView.getItems().clear();
            clientView.getItems().addAll(Files.list(clientFileTreeDir).map(Path::getFileName).map(Path::toString).toList());
            serverCM.hide();
        }
        serverView.getSelectionModel().clearSelection();
        clientView.getSelectionModel().clearSelection();
    }

    public void btnAuthClick(ActionEvent actionEvent) throws IOException {
        String login = loginField.getText();
        String password = passwordField.getText();
        String authData = login + "#" + password;
        // send "coded" authentication data
        net.write(new AuthMessage(authData));
        log.info("sent request to authenticate...");
    }

    public void cancelAuthClick(ActionEvent actionEvent) {
        log.info("user closed the program by pressing cancel button...");
        System.exit(33);
    }

    public void rename(ActionEvent actionEvent) {
        // make visible textArea for new file name
        // make visible button to confirm the change
        if (serverView.getSelectionModel().getSelectedItem() == null) {
            changeFileNamePane.setLayoutX(275);
            changeFileNamePane.setLayoutY(525);
            changeFileNamePane.setVisible(true);
            changeFileNamePane.toFront();
        }
        if (clientView.getSelectionModel().getSelectedItem() == null) {
            changeFileNamePane.setLayoutX(740);
            changeFileNamePane.setLayoutY(525);
            changeFileNamePane.setVisible(true);
            changeFileNamePane.toFront();
        }

    }

    public void confirmFileNameChange(ActionEvent actionEvent) throws IOException {
        String serverFileToRename = serverView.getSelectionModel().getSelectedItem();
        String localFileToRename = clientView.getSelectionModel().getSelectedItem();
        String newFileName = newFileNameField.getText();

        if (localFileToRename == null) {

            if (serverFileTreeDir == null) {
                String toRename = "null" + "#" + serverFileToRename + "#" + newFileName;
                net.write(new ChangeFileNameMessage(toRename));
            } else {
                String toRename = serverFileTreeDir + "#" + serverFileToRename + "#" + newFileName;
                net.write(new ChangeFileNameMessage(toRename));
            }

            changeFileNamePane.setVisible(false);
            newFileNameField.clear();
        }

        if (serverFileToRename == null) {
            if (clientFileTreeDir == null) {
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
                    changeFileNamePane.setVisible(false);
                    newFileNameField.clear();
                    reloadList();
                } else {
                    System.out.println("no data given");
                }
            } else {
                File originalFile = new File(String.valueOf(clientFileTreeDir), localFileToRename);
                File newFile = new File(String.valueOf(clientFileTreeDir), newFileName);
                if (localFileToRename != null ) {
                    if (newFile.exists()) {
                        System.out.println("file exists already");
                    }
                    boolean successFileNameChange = originalFile.renameTo(newFile);

                    if (!successFileNameChange) {
                        System.out.println("Something went wrong, cannot rename a file");
                    }
                    changeFileNamePane.setVisible(false);
                    newFileNameField.clear();
                    clientView.getItems().clear();
                    clientView.getItems().addAll(Files.list(clientFileTreeDir).map(Path::getFileName).map(Path::toString).toList());
                } else {
                    System.out.println("no data given");
                }
            }
        }
    }

    public void dropSelectionOnClientList(MouseEvent mouseEvent) {
        clientView.getSelectionModel().clearSelection();
    }

    public void dropSelectionOnServerList(MouseEvent mouseEvent) {
        serverView.getSelectionModel().clearSelection();
    }

    public void createNewFolder(ActionEvent actionEvent) {
        if (serverView.getSelectionModel().getSelectedItem() == null) {
            createNewFolderPane.setLayoutX(155);
            createNewFolderPane.setLayoutY(525);
            createNewFolderPane.setVisible(true);
            createNewFolderPane.toFront();
        }
        if (clientView.getSelectionModel().getSelectedItem() == null) {
            createNewFolderPane.setLayoutX(635);
            createNewFolderPane.setLayoutY(525);
            createNewFolderPane.setVisible(true);
            createNewFolderPane.toFront();
        }

    }

    public void confirmFolderNameChange(ActionEvent actionEvent) throws IOException {
        String serverSideSelected = serverView.getSelectionModel().getSelectedItem();
        String localSideSelected = clientView.getSelectionModel().getSelectedItem();
        String newFolderName = newFolderNameField.getText();

        if (serverSideSelected == null && localSideSelected == null) {
            System.out.println("Non of the sides has been selected to create a folder");
            newFolderNameField.clear();
            createNewFolderPane.setVisible(false);
            confirmFileNameChange(actionEvent);
        }

        if (serverSideSelected == null) {

            if (clientFileTreeDir == null) {
                String toCreateFolder = "LocalFiles" + "/" + newFolderName;
                File newFolder = new File(toCreateFolder);

                boolean bool = newFolder.mkdir();
                if(bool){
                    System.out.println("Folder is created successfully");
                }else{
                    System.out.println("Something went wrong. Could not create a directory");
                }
                newFolderNameField.clear();
                createNewFolderPane.setVisible(false);
                reloadList();
            } else {
                String toCreateFolder = clientFileTreeDir + "/" + newFolderName;
                File newFolder = new File(toCreateFolder);

                boolean bool = newFolder.mkdir();
                if(bool){
                    System.out.println("Folder is created successfully");
                }else{
                    System.out.println("Something went wrong. Could not create a directory");
                }
                newFolderNameField.clear();
                createNewFolderPane.setVisible(false);
                clientView.getItems().clear();
                clientView.getItems().addAll(Files.list(clientFileTreeDir).map(Path::getFileName).map(Path::toString).toList());
            }
        }

        if (localSideSelected == null) {

            if (serverFileTreeDir == null) {
                String newServerDir = "null" + "@" + newFolderName;
                net.write(new createNewDirMessage(newServerDir));
                newFolderNameField.clear();
                createNewFolderPane.setVisible(false);
            } else {
                String newServerDir = serverFileTreeDir + "@" + newFolderName;
                net.write(new createNewDirMessage(newServerDir));
                newFolderNameField.clear();
                createNewFolderPane.setVisible(false);
            }
        }
    }

    public void move(ActionEvent actionEvent) {
        clientSelectedFileToMove = clientView.getSelectionModel().getSelectedItem();
        ServerSelectedFileToMove = serverView.getSelectionModel().getSelectedItem();
        // getting the directories of files before move
        initialLocalDir = String.valueOf(clientFileTreeDir);
        initialServerDir = String.valueOf(serverFileTreeDir);
        confirmMoveButton.setVisible(true);
//        moveButton.setVisible(false);
    }

    public void moveHere(ActionEvent actionEvent) throws IOException {
        // name of the files to be moved
        String localFileToMove = clientSelectedFileToMove;
        String serverFileToMove = ServerSelectedFileToMove;
        // saving the initial directories of the files
        String localFirstDir = initialLocalDir + "/" + localFileToMove;
        String serverFirstDir = initialServerDir;
        // getting the desired directories where files should be moved
        String localFinalDir = String.valueOf(clientFileTreeDir) + "/" + localFileToMove;
        String serverFinalDir = String.valueOf(serverFileTreeDir);

        if (serverFileToMove == null) {

            if (initialLocalDir == null) {
                System.out.println("FROM LOCALFILES: " + "LocalFiles" + "/" + localFileToMove);
                File localFile1 = new File("LocalFiles" + "/" + localFileToMove);
                localFile1.renameTo(new File(localFinalDir));
                reloadList();
                confirmMoveButton.setVisible(false);
                moveButton.setVisible(true);
            } else {
                if (localFirstDir.contains("null")) {
                    System.out.println("localFirstDir contains null: " + localFirstDir);
                    File localFile1 = new File("LocalFiles" + "/" + localFileToMove);
                    localFile1.renameTo(new File(localFinalDir));
                    clientView.getItems().clear();
                    clientView.getItems().addAll(Files.list(clientFileTreeDir).map(Path::getFileName).map(Path::toString).toList());
                    confirmMoveButton.setVisible(false);
                    moveButton.setVisible(true);
                } else {
                    File localFile1 = new File(localFirstDir);
                    localFile1.renameTo(new File(localFinalDir));
                    System.out.println("FROM: " + localFirstDir + " TO " + localFinalDir);
                    clientView.getItems().clear();
                    clientView.getItems().addAll(Files.list(clientFileTreeDir).map(Path::getFileName).map(Path::toString).toList());
                    confirmMoveButton.setVisible(false);
                    moveButton.setVisible(true);
                }
            }
        }

        if (localFileToMove == null) {

            if (serverFirstDir == null) {
                String toSendMove = "null" + "#" + serverFinalDir + "#" + serverFileToMove;
                net.write(new moveMessage(toSendMove));
                confirmMoveButton.setVisible(false);
                moveButton.setVisible(true);
            } else {
                String toSendMove = serverFirstDir + "#" + serverFinalDir + "#" + serverFileToMove;
                net.write(new moveMessage(toSendMove));
                confirmMoveButton.setVisible(false);
                moveButton.setVisible(true);
            }
        }
    }

    public void localContextMenuRequested(ContextMenuEvent contextMenuEvent) {
        System.out.println("OMAGAD LOCAL CONTEXT MENU REQUESTED");
    }

    public void serverContextMenuRequested(ContextMenuEvent contextMenuEvent) {
        System.out.println("OMAGAD SERVER CONTEXT MENU REQUESTED");
    }
}