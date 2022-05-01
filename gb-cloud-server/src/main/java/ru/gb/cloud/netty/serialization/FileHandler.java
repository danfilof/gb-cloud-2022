package ru.gb.cloud.netty.serialization;


import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import ru.gb.cloud.model.AbstractMessage;
import ru.gb.cloud.model.FileMessage;
import ru.gb.cloud.model.ListMessage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class FileHandler extends SimpleChannelInboundHandler<AbstractMessage> {

    private final Path serverDir = Path.of("ServerFiles");

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(new ListMessage(serverDir));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AbstractMessage msg) throws Exception {
        log.info("received: {} message", msg.getMessageType().getName());
        if (msg instanceof FileMessage file) {
            Files.write(serverDir.resolve(file.getName()), file.getBytes());
        }
    }
}
