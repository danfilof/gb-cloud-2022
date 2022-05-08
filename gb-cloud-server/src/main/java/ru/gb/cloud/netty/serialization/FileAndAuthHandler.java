package ru.gb.cloud.netty.serialization;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import ru.gb.cloud.model.*;
;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;

@Slf4j
public class FileAndAuthHandler extends SimpleChannelInboundHandler<AbstractMessage> {

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
            String downloadFile = downloadMessage.getDownloadFileName();
            System.out.println("downloadMsg: " + downloadFile);
            ctx.write(new FileMessage(serverDir.resolve(downloadFile)));
            ctx.writeAndFlush(new ListMessage(serverDir));
        }

        if (msg instanceof DeleteMessage deleteMessage) {
            String deleteFile = deleteMessage.getDeleteFileName();
            System.out.println("deleteMsg: " + deleteFile);
            Path toDelete = Path.of("ServerFiles", deleteFile);
            Files.deleteIfExists(toDelete);
            ctx.writeAndFlush(new ListMessage(serverDir));
        }

        if (msg instanceof AuthMessage authMessage) {
            String authData = authMessage.getAuthData();
            String[] split = authData.split("#");
            String login = split[0];
            String password = split[1];
            String status = getStatusByLoginAndPassword(login,password);
            ctx.write(new AuthMessage(status));
            ctx.writeAndFlush(new ListMessage(serverDir));
        }

        if (msg instanceof FileMessage file) {
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
