package ru.gb.cloud.netty.serialization;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import ru.gb.cloud.model.AbstractMessage;
import ru.gb.cloud.model.FileMessage;
import ru.gb.cloud.model.ListMessage;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class FileHandler extends SimpleChannelInboundHandler<AbstractMessage> {

    private final Path serverDir = Path.of("ServerFiles");

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ListMessage message = new ListMessage(serverDir);
        ctx.writeAndFlush(new ListMessage(serverDir));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AbstractMessage msg) throws Exception {
        log.info("received: {} message", msg.getMessageType().getName());
        if (msg instanceof FileMessage file) {

            // the handler receives the file, which has name "command_download.txt". Inside of the file there is a name of the file that should be downloaded
            if (((FileMessage) msg).getName().equals("command_download.txt")) {
                // download the file
                Files.write(serverDir.resolve(file.getName()), file.getBytes());
                // read the contents into byte array
                byte[] encoded = Files.readAllBytes(Paths.get("ServerFiles/command_download.txt"));
                // write bytearray into string and send the file with a given fileName as a string from bytearray
                String fileName = new String(encoded, StandardCharsets.UTF_8);
                ctx.write(new FileMessage(serverDir.resolve(fileName)));
            }
            Files.write(serverDir.resolve(file.getName()), file.getBytes());
            ctx.writeAndFlush(new ListMessage(serverDir));
        }
    }
}
