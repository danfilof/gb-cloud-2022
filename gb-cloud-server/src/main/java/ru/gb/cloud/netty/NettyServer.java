package ru.gb.cloud.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import ru.gb.cloud.netty.serialization.SerializationPipeLine;

@Slf4j
public class NettyServer {

    public static void main(String[] args) {
        ServerBootstrap bootstrap = new ServerBootstrap();
        EventLoopGroup light = new NioEventLoopGroup(1);
        EventLoopGroup hard = new NioEventLoopGroup();
        try {
            bootstrap.group(light, hard).channel(NioServerSocketChannel.class).childHandler(new SerializationPipeLine());

            ChannelFuture channelFuture = bootstrap.bind(8189).sync();
            log.info("Server started...");

            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            log.error("", e);
        } finally {
            light.shutdownGracefully();
            hard.shutdownGracefully();
        }
    }
}
