package com;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONObject;


import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileMessageHandler extends SimpleChannelInboundHandler<Command> {

    private final Path serverPath = Paths.get("C:\\Users\\Дмитрий\\Desktop\\coursWork-main\\Server\\root");
    private Path serverPathForClients = Paths.get("Server","root\\");
    private Path currentPath = Paths.get("Server","root");
    private final Path logPath = Paths.get("Server", "log");
    private Path clientPath ;
    private final String jsonFile = "lohJson.json";

    private SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yyyy 'в' HH:mm:ss");
    private Date date = new Date();



    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.debug("Client connected!");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Command cmd) throws Exception {
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
                AuthRequest authRequest = (AuthRequest) cmd;
                String login = authRequest.getLogin();
                String password = authRequest.getPassword();
                JSONObject jsonObject = new JSONObject(Files.readString(logPath.resolve(jsonFile)));
                String post;
                String main1,main2="";
                AuthResponse authResponse = new AuthResponse();
                if (jsonObject.has(login)) {
                    if (jsonObject.getJSONObject(login).getString("Password").equals(password)) {
                        post = jsonObject.getJSONObject(login).getString("Post");
                        main1 =jsonObject.getJSONObject(login).getString("Main");
                        if(!main1.equals("")) {
                            main2=jsonObject.getJSONObject(main1).getString("Main");
                            if(!main2.equals("")){
                            main2=jsonObject.getJSONObject(main1).getString("Main");
                            }
                        }
                        System.out.println(post);
                        authResponse.setAuthStatus(true);
                        authResponse.setPost(post);
                        switch (post){
                            case "Admin"->{
                                clientPath = serverPath;
                                if (!Files.exists(clientPath)) {
                                    Files.createDirectory(clientPath);
                                }
                                currentPath = clientPath;
                            }
                            case "ChiefEditor"->{
                                clientPath= serverPathForClients.resolve(login);
                                if (!Files.exists(clientPath)) {
                                    Files.createDirectory(clientPath);
                                }
                                currentPath = clientPath;
                            }
                            case "DepartmentEditor"->{
                                clientPath = serverPathForClients.resolve(main1+"\\"+login);
                                if (!Files.exists(clientPath)) {
                                    Files.createDirectory(clientPath);
                                }
                                currentPath = clientPath;
                            }
                            case "Author" ->{
                                clientPath = serverPathForClients.resolve(main2+"\\"+main1+"\\"+login);
                                if (!Files.exists(clientPath)) {
                                    Files.createDirectory(clientPath);
                                }
                                currentPath = clientPath;
                            }
                        }
//                        clientPath = serverPath.resolve(login);
//                        if (!Files.exists(clientPath)) {
//                            Files.createDirectory(clientPath);
//                        }
//                        currentPath = clientPath;
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
                if (currentPath.getParent() != null) {//убрал права, так как кнопка доступна только для админа
//                    if (currentPath.equals(clientPath)) {//блок по правам
//                        log.debug("Above the client's folder , it is not necessary to rise");
//                    } else {
                        currentPath = currentPath.getParent();
//                    }
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
            case UPDATE_DATE_FILE_REQUEST -> {
                List<String> results = new ArrayList<>();
                File[] files = new File(String.valueOf(currentPath)).listFiles();
                for (File file : files) {
                    SimpleDateFormat sdf1 = new SimpleDateFormat("dd.MM.yyyy 'в' HH:mm:ss");
                    String format = sdf1.format(file.lastModified());
                    results.add(format);
                }
                ctx.writeAndFlush(new UpdateDateFileResponse(results));
//                if(results.equals(sdf.format(date))){//todo!!!1
////                    results.add("New "+format);
////                }else {
////                    results.add(format);
////                }
            }
            default -> log.debug("Invalid command {}", cmd.getType());
        }
    }
}