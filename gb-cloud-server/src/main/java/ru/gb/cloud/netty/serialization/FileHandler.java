package ru.gb.cloud.netty.serialization;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import ru.gb.cloud.model.AbstractMessage;
import ru.gb.cloud.model.DownloadMessage;
import ru.gb.cloud.model.FileMessage;
import ru.gb.cloud.model.ListMessage;;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Objects;

@Slf4j
public class FileHandler extends SimpleChannelInboundHandler<AbstractMessage> {

    private Connection connection;
    private Statement statement;


    private final Path serverDir = Path.of("ServerFiles");

    private final Path auth = Path.of("ServerFiles/command_auth.txt");

    private final Path cmd = Path.of("ServerFiles/cmd");



    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ListMessage message = new ListMessage(serverDir);
        ctx.writeAndFlush(new ListMessage(serverDir));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AbstractMessage msg) throws Exception {
        log.info("received: {} message", msg.getMessageType().getName());

        // NEW NEW NEW NEW
        if (msg instanceof DownloadMessage downloadMessage) {
            String s = downloadMessage.getDownloadFileName();
            System.out.println("downloadMsg: " + s);
        }



        // NEW NEW NEW NEW
        if (msg instanceof FileMessage file) {
            // the handler receives the file, which has name "command_download.txt". Inside of the file there is a name of the file that should be downloaded
            if (((FileMessage) msg).getName().equals("command_download.txt")) {
                // download the file
                Files.write(serverDir.resolve(file.getName()), file.getBytes());
                // read the contents into byte array
                byte[] encodedDownload = Files.readAllBytes(Paths.get("ServerFiles/cmd/command_download.txt"));
                // write bytearray into string and send the file with a given fileName as a string from bytearray
                String fileName = new String(encodedDownload, StandardCharsets.UTF_8);
                ctx.write(new FileMessage(serverDir.resolve(fileName)));
                ctx.writeAndFlush(new ListMessage(serverDir));
            }

            if (((FileMessage) msg).getName().equals("command_delete.txt")) {
                //download the file
                Files.write(serverDir.resolve(file.getName()), file.getBytes());
                // read the content into byte array
                byte[] encodedDelete = Files.readAllBytes(Paths.get("ServerFiles/command_delete.txt"));
                // get a fileName from the file
                String deleteFile = new String(encodedDelete, StandardCharsets.UTF_8);
                // delete the selected file
                Path toDelete = Path.of("ServerFiles", deleteFile);
                Files.deleteIfExists(toDelete);
                ctx.writeAndFlush(new ListMessage(serverDir));


            }

                if (((FileMessage) msg).getName().equals("command_auth.txt")) {
                    //download the file
                    Files.write(serverDir.resolve(file.getName()), file.getBytes());
                    // read the content into byte array
                    byte[] encodedAuth = Files.readAllBytes(Paths.get("ServerFiles/command_auth.txt"));
                    // get a login and password from the file
                    String authData = new String(encodedAuth, StandardCharsets.UTF_8);
                    // split the message and get the data
                     String[] split = authData.split("#");
                     String login = split[0];
                     String password = split[1];
                    // clear the file
                    String status = getStatusByLoginAndPassword(login,password);
                    BufferedWriter writer = Files.newBufferedWriter(Paths.get(String.valueOf(auth)));
                    writer.write("");
                    writer.flush();

                    // send a file with a status written in
                    Files.writeString(auth,status, StandardCharsets.UTF_8);
                    ctx.write(new FileMessage(serverDir.resolve("command_auth.txt")));
                }

           Files.write(serverDir.resolve(file.getName()), file.getBytes());
            ctx.writeAndFlush(new ListMessage(serverDir));
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
            status = rs.getString("id");
            log.info("Status after db: " + status);
            if (status != null) {
                System.out.println("Status in db: " + status);
                return status = "%OK";
            } else {
                System.out.println("Status in db: " + status);
                return status = "%WrongData";
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void connect() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:gb_chat_db.db");
            statement = connection.createStatement();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
