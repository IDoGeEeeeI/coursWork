package com;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.json.JSONObject;


import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileMessageHandler extends SimpleChannelInboundHandler<Command> {

    private Path currentPath = Paths.get("Server","root");
    private Path clientPath ;


    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.debug("Client connected!");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Command cmd) throws Exception {
//        System.out.println( Files.readString(Paths.get("Server", "log", "lohJson.json")));//чтение json
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
            case FILE_DELETED_REQUEST:
                FileDeleteRequest fileDeleteRequest = (FileDeleteRequest) cmd;
                String fileN = fileDeleteRequest.getName();
                File fileD = new File("Server/root/"+fileN);
                    fileD.delete();
                    System.out.println(fileN+ " deleted..");
                    ctx.writeAndFlush(new ListResponse(currentPath));
                    ctx.writeAndFlush(new PathResponse(currentPath.toString()));
            case AUTH_REQUEST:
                AuthRequest authRequest = (AuthRequest) cmd; // TODO: 04.11.2021 Casting 'cmd' to 'AuthRequest' may produce 'ClassCastException' 
                String login = authRequest.getLogin();
                String password = authRequest.getPassword();
                JSONObject jsonObject = new JSONObject(Files.readString(Paths.get("Server", "log", "lohJson.json")));
                String post;
                AuthResponse authResponse = new AuthResponse();
                if(jsonObject.has(login)){
                    if(jsonObject.getJSONObject(login).getString("Password").equals(password)){
                        post  = jsonObject.getJSONObject(login).getString("Post");
                        System.out.println(post);
                        authResponse.setAuthStatus(true);
                        authResponse.setPost(post);
                        clientPath = Paths.get("/Users/dmitrijpankratov/Desktop/coursework/Server", login);
                        if (!Files.exists(clientPath)) {
                            Files.createDirectory(clientPath);
                        }
                        currentPath = clientPath;
                    }else {
                        authResponse.setAuthStatus(false);
                    }
                }else {
                    authResponse.setAuthStatus(false);
                }
                ctx.writeAndFlush(authResponse);
//            System.out.println( Files.readString(Paths.get("Client", "root", "lohJson.json")));//чтение json
                break;
            default:
                log.debug("Invalid command {}", cmd.getType());
                break;
        }
    }
}