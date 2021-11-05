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

    private Path serverPath = Paths.get("/Users/dmitrijpankratov/Desktop/coursework/Server");
    private Path currentPath = Paths.get("Server","root");
    private Path logPath = Paths.get("Server", "log");
    private Path clientPath ;
    private String jsonFile = "lohJson.json";



    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.debug("Client connected!");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Command cmd) throws Exception {
//        System.out.println( Files.readString(Paths.get("Server", "log", "lohJson.json")));//чтение json
        log.debug("Received command from client: {}", cmd.getType());
        switch (cmd.getType()) {
            case FILE_MESSAGE -> {
                FileMessage fileMessage = (FileMessage) cmd;
                Files.write(
                        currentPath.resolve(fileMessage.getName()),
                        fileMessage.getBytes()
                );
                ctx.writeAndFlush(new ListResponse(currentPath));
                log.debug("Received a file {} from the client", fileMessage.getName());
            }
            case FILE_REQUEST -> {
                FileRequest fileRequest = (FileRequest) cmd;
                String fileName = fileRequest.getName();
                Path file = Paths.get(String.valueOf(currentPath), fileName);
                ctx.writeAndFlush(new FileMessage(file));
                log.debug("Send file {} to the client", fileName);
            }
            case LIST_REQUEST -> {
                ctx.writeAndFlush(new ListResponse(currentPath));
                ctx.writeAndFlush(new PathResponse(currentPath.toString()));
                log.debug("Send list of files to the client");
            }
            case FILE_DELETED_REQUEST -> {
                FileDeleteRequest fileDeleteRequest = (FileDeleteRequest) cmd;
                String fileN = fileDeleteRequest.getName();
                File fileD = new File(String.valueOf(currentPath.resolve(fileN)));
                fileD.delete();
                System.out.println(fileN + " deleted..");
                ctx.writeAndFlush(new ListResponse(currentPath));
                ctx.writeAndFlush(new PathResponse(currentPath.toString()));
            }
            case AUTH_REQUEST -> {
                //            System.out.println( Files.readString(Paths.get("Client", "root", "lohJson.json")));//чтение json
                AuthRequest authRequest = (AuthRequest) cmd;
                String login = authRequest.getLogin();
                String password = authRequest.getPassword();
                JSONObject jsonObject = new JSONObject(Files.readString(logPath.resolve(jsonFile)));
                String post;
                AuthResponse authResponse = new AuthResponse();
                if (jsonObject.has(login)) {
                    if (jsonObject.getJSONObject(login).getString("Password").equals(password)) {
                        post = jsonObject.getJSONObject(login).getString("Post");
                        System.out.println(post);
                        authResponse.setAuthStatus(true);
                        authResponse.setPost(post);
                        clientPath = serverPath.resolve(login);
                        if (!Files.exists(clientPath)) {
                            Files.createDirectory(clientPath);
                        }
                        currentPath = clientPath;
                    } else {
                        authResponse.setAuthStatus(false);
                    }
                } else {
                    authResponse.setAuthStatus(false);
                }
                ctx.writeAndFlush(authResponse);
            }
            case PATH_IN_REQUEST -> {
                PathInRequest request = (PathInRequest) cmd;
                Path newPAth = currentPath.resolve(request.getDir());
                if (Files.isDirectory(newPAth)) {
                    currentPath = newPAth;
                    log.debug("Send list of files and current directory to the client");
                    ctx.writeAndFlush(new ListResponse(currentPath));
                    ctx.writeAndFlush(new PathResponse(currentPath.toString()));
                } else {
                    log.debug("{} is not a directory",request);
                }
            }
            case PATH_UP_REQUEST -> {
                if (currentPath.getParent() != null) {
                    if (currentPath.equals(clientPath)) {//блок по правам // TODO: 05.11.2021 !!!
                        log.debug("Above the client's folder , it is not necessary to rise");
                    } else {
                        currentPath = currentPath.getParent();
                    }
                }
                log.debug("Send list of files and current directory to the client");
                ctx.writeAndFlush(new ListResponse(currentPath));
                ctx.writeAndFlush(new PathResponse(currentPath.toString()));
            }
            case AUTH_OUT_REQUEST->{
                AuthOutResponse authOutResponse = new AuthOutResponse();
                authOutResponse.setAuthOutStatus(false);
                ctx.writeAndFlush(new AuthOutResponse());
            }
            default -> log.debug("Invalid command {}", cmd.getType());
        }
    }
}