package ru.gb.cloud.controllers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import ru.gb.cloud.model.AbstractMessage;
import ru.gb.cloud.model.FileMessage;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class LocalFileHandler extends SimpleChannelInboundHandler<AbstractMessage> {

    private MainController mainController;

    private final Path localDir = Path.of("LocalFiles");
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AbstractMessage msg) throws Exception {
        log.info("received: {} message", msg.getMessageType().getName());
        if (msg instanceof FileMessage file) {
            Files.write(localDir.resolve(file.getName()), file.getBytes());
            mainController.reloadList();
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // reloads the Client list
       mainController.reloadList();
    }
}
