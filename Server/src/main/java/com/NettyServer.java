package com;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.Locale;
import java.util.Scanner;

// TODO: 19.11.2021 правильное закрытие по команде сделать
@Slf4j
public class NettyServer {
    public NettyServer() {

        EventLoopGroup auth = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            ChannelFuture channelFuture = bootstrap.group(auth, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline().addLast(
                                    new ObjectEncoder(),
                                    new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                                    new FileMessageHandler()
                            );
                        }
                    })
                    .bind(8189)
                    .sync();
            log.debug("Server started...");
            //
            Scanner scr = new Scanner(System.in);
            String str = scr.next();
            if ("stop".equals(str.toLowerCase(Locale.ROOT))) {// TODO: 19.11.2021 не уверен насчет этой реализации
                channelFuture.channel().close();
            }
            //
            channelFuture.channel().closeFuture().sync(); // block
        }catch (Exception e) {
            log.error("Server exception: Stacktrace: ", e);
        } finally {
            auth.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        new NettyServer();
    }
}
