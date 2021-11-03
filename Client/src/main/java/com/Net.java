package com;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Net {

    private static Net INSTANCE;

    private final Callback callback;
    private SocketChannel channel;

    public static Net getInstance(Callback callback) {
        if (INSTANCE == null) {
            INSTANCE = new Net(callback);
        }
        return INSTANCE;
    }

    private Net(Callback callback) {
        this.callback = callback;

        Thread thread = new Thread(() -> {
            EventLoopGroup group = new NioEventLoopGroup();

            try {
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(group)
                        .channel(NioSocketChannel.class)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel c) throws Exception {
                                channel = c;
                                channel.pipeline().addLast(
                                        new ObjectEncoder(),
                                        new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                                        new ClientCommandHandler(callback)
                                );
                            }
                        });

                ChannelFuture future = bootstrap.connect("localhost", 8189).sync();
                log.debug("Client Connected..");
//                sendCommand(new ListRequest());
                future.channel().closeFuture().sync(); // block
            } catch (Exception e) {
                log.error("", e);
            } finally {
                group.shutdownGracefully();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public void sendCommand(Command cmd) {
        channel.writeAndFlush(cmd);
    }

}