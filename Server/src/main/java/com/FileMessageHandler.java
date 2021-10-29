package com;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileMessageHandler extends SimpleChannelInboundHandler<Command> {

    private Path currentPath = Paths.get("Server","root");
    private Path clientPath;


    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.debug("Client connected!");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Command cmd) throws Exception {
        log.debug("Received command from client: {}", cmd.getType());
        switch (cmd.getType()) {
            case FILE_MESSAGE:
                FileMessage fileMessage = (FileMessage) cmd;
                Files.write(
                        currentPath.resolve(fileMessage.getName()),
                        fileMessage.getBytes()
                );
                ctx.writeAndFlush(new ListResponse(currentPath));
                log.debug("Received a file {} from the client", fileMessage.getName());
                break;

            case FILE_REQUEST:
                FileRequest fileRequest = (FileRequest) cmd;
                String fileName = fileRequest.getName();
                Path file = Paths.get(String.valueOf(currentPath), fileName);
                ctx.writeAndFlush(new FileMessage(file));
                log.debug("Send file {} to the client", fileName);

                break;

            case LIST_REQUEST:
                ctx.writeAndFlush(new ListResponse(currentPath));
                ctx.writeAndFlush(new PathResponse(currentPath.toString()));
                log.debug("Send list of files to the client");
                break;

            default:
                log.debug("Invalid command {}", cmd.getType());
                break;
        }
    }
}