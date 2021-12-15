package com;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
/**
 * Клас обработчик команд.
 */
@Slf4j
public class ClientCommandHandler extends SimpleChannelInboundHandler<Command> {

    private final Callback callback;

    public ClientCommandHandler(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Command cmd) throws Exception {
        log.debug("received {}", cmd.getType());
        callback.call(cmd);//паттерн отложенной реализации
    }
}
