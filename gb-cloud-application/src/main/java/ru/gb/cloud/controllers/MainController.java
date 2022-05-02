package ru.gb.cloud.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import lombok.extern.slf4j.Slf4j;
import ru.gb.cloud.model.AbstractMessage;
import ru.gb.cloud.model.FileMessage;
import ru.gb.cloud.model.ListMessage;
import ru.gb.cloud.network.Net;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.FileHandler;

@Slf4j
public class MainController implements Initializable {
    public ListView<String> clientView;
    public ListView<String> serverView;
    private Net net;
   private Path clientDir;

   // folder where the "download_command.txt" is
   private final Path downloadDir = Path.of("Download command file");

   // exact path of "download_command.txt"
   private final Path fileDownDir = Path.of("Download command file/command_download.txt");

    private void read() {
        try {
            while (true) {
                AbstractMessage message = net.read();
                if (message instanceof ListMessage lm) {
                    serverView.getItems().clear();
                    serverView.getItems().addAll(lm.getFiles());
                }
                if (message instanceof FileMessage file) {
                    // if the message is FileMessage, download ther file and reload the list
                    Files.write(clientDir.resolve(file.getName()), file.getBytes());
                    clientView.getItems().clear();
                    clientView.getItems().addAll(getClientFiles());
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
        // choosing a fileName with a click in jfx
        String fileName = serverView.getSelectionModel().getSelectedItem();
        // writes the name of the file to be downloaded into the "command_download.txt"
        Files.writeString(fileDownDir, fileName, StandardCharsets.UTF_8);
        // sends the "command_download.txt" as a FileMessage
        net.write(new FileMessage(downloadDir.resolve("command_download.txt")));
        //clears the "command_download.txt" file for a further usage
        BufferedWriter writer = Files.newBufferedWriter(Paths.get(String.valueOf(fileDownDir)));
        writer.write("");
        writer.flush();
    }

    public void reloadList() throws IOException {
        clientView.getItems().clear();
        clientView.getItems().addAll(getClientFiles());
    }

}
