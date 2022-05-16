package ru.gb.cloud.netty.serialization;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import ru.gb.cloud.model.*;


import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.List;

@Slf4j
public class FileAndAuthHandler extends SimpleChannelInboundHandler<AbstractMessage> {

    private Connection connection;
    private Statement statement;
    private final Path serverDir = Path.of("ServerFiles");

    private Path requestedDir;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(new ListMessage(serverDir));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AbstractMessage msg) throws Exception {
        log.info("received: {} message", msg.getMessageType().getName());

        if (msg instanceof DownloadMessage downloadMessage) {
            String downloadFile = downloadMessage.getDownloadFileName();
            String[] downloadSplit = downloadFile.split("%");
            String downloadDir = downloadSplit[0];
            String fileToDownload = downloadSplit[1];
            Path downloadPath = Path.of(downloadDir);
            log.info("received request to download {}", downloadFile);
            if (downloadDir.equals("null")){
                ctx.write(new FileMessage(serverDir.resolve(fileToDownload)));
            } else {
                ctx.write(new FileMessage(downloadPath.resolve(fileToDownload)));
            }
            ctx.writeAndFlush(new ListMessage(serverDir));
        }

        if (msg instanceof DeleteMessage deleteMessage) {
            String deleteFile = deleteMessage.getDeleteFileName();
            String[] deleteSplit = deleteFile.split("%");
            String deleteDir = deleteSplit[0];
            String fileToDelete = deleteSplit[1];
            log.info("received request to delete {} file", fileToDelete);

            if (deleteDir.equals("null")){
                Path toDelete = Path.of("ServerFiles", fileToDelete);
                Files.deleteIfExists(toDelete);
                ctx.writeAndFlush(new ListMessage(serverDir));
            } else {
                Path toBeDeleted = Path.of(deleteDir, fileToDelete);
                Files.deleteIfExists(toBeDeleted);
                Path toSend = Path.of(deleteDir);
                ctx.writeAndFlush(new ListMessage(toSend));
            }
        }

        if (msg instanceof AuthMessage authMessage) {
            log.info("received authenticate request...");
            String authData = authMessage.getAuthData();
            // get user login and password from message, split them
            String[] split = authData.split("#");
            String login = split[0];
            String password = split[1];
            String status = getStatusByLoginAndPassword(login,password);
            ctx.write(new AuthMessage(status));
            ctx.writeAndFlush(new ListMessage(serverDir));
        }

        if (msg instanceof ChangeFileNameMessage changeFileNameMessage) {
            log.info("received request to rename a file");
            String fileNames = changeFileNameMessage.getFileNames();
            String[] splitFileNames = fileNames.split("#");
            String changeFileNameDir = splitFileNames[0];
            String originalName = splitFileNames[1];
            String newName = splitFileNames[2];
            log.info(changeFileNameDir + " || " + originalName + " || " + newName);

            if (changeFileNameDir.equals("null")) {
                File originalFile = new File("ServerFiles", originalName);
                File newFile = new File("ServerFiles", newName);

                if (newFile.exists()) {
                    log.info("file exists already");
                }
                boolean successFileNameChange = originalFile.renameTo(newFile);

                if (!successFileNameChange) {
                    log.info("Something went wrong, cannot rename a file");
                }
                ctx.writeAndFlush(new ListMessage(serverDir));
            } else {
                File originalFile = new File(changeFileNameDir, originalName);
                File newFile = new File(changeFileNameDir, newName);
                if (newFile.exists()) {
                    log.info("file exists already");
                }
                boolean successFileNameChange = originalFile.renameTo(newFile);

                if (!successFileNameChange) {
                    log.info("Something went wrong, cannot rename a file");
                }
                Path changedNamePath = Path.of(changeFileNameDir);
                ctx.writeAndFlush(new ListMessage(changedNamePath));
            }

        }

        if (msg instanceof FileMessage file) {
            log.info("received file {}", file.getName());
           Files.write(serverDir.resolve(file.getName()), file.getBytes());
            ctx.writeAndFlush(new ListMessage(serverDir));
        }

        if (msg instanceof directoryMessage directoryMessage) {
            log.info("received directory: " + directoryMessage.getDirectString());
            String reqStr = directoryMessage.getDirectString();
            System.out.println("reqStr: " + reqStr);
            requestedDir = Path.of(reqStr);
            log.info("requested dir: " + requestedDir);
            List<String> testDir =  Files.list(requestedDir).map(Path::getFileName).map(Path::toString).toList();
            log.info("testDir: " + testDir);
           ctx.writeAndFlush(new ListMessage(requestedDir));
        }

        if (msg instanceof createNewDirMessage createNewDirMessage) {
            log.info("received command to create a new directory");
            String newFolderDir = createNewDirMessage.getNewDir();
            String[] splitFolderDir = newFolderDir.split("@");
            String directory = splitFolderDir[0];
            String newDir = splitFolderDir[1];

            if (directory.equals("null")) {
                String newActualFolder = "ServerFiles" + "/" + newDir;
                File newFolder = new File(newActualFolder);

                boolean bool = newFolder.mkdir();
                if(bool){
                    System.out.println("Folder is created successfully");
                }else{
                    System.out.println("Something went wrong. Could not create a directory");
                }
                ctx.writeAndFlush(new ListMessage(serverDir));
            } else {
                String newActualFolder = directory + "/" + newDir;
                File newFolder = new File(newActualFolder);

                boolean bool = newFolder.mkdir();
                if(bool){
                    System.out.println("Folder is created successfully");
                }else{
                    System.out.println("Something went wrong. Could not create a directory");
                }
                Path path = Path.of(directory);
                ctx.writeAndFlush(new ListMessage(path));
            }
        }
    }
    public  String getStatusByLoginAndPassword(String login, String password) {
        connect();
        String status = checkAuth(login, password);
        return status;
    }

    public String checkAuth(String login, String password) {
        try (PreparedStatement ps = connection.prepareStatement("select id from users where login = ? and pass = ?")){
            ps.setString(1, login);
            ps.setString(2, password);
            String id = null;
            String status = null;
            ResultSet rs = ps.executeQuery();
            int idCheck = rs.getInt("id");
            log.info("data base query has been executed...");
            if (idCheck == (int) idCheck) {
                return status = "%OK";
            } else {
                return status = "%WrongData";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "wrongData";
        }
    }
    public void connect() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:gb_chat_db.db");
            statement = connection.createStatement();
            log.info("successfully connected to dataBase");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
