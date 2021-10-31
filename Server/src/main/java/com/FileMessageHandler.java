package com;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.json.JSONObject;


import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.json.JsonObjectDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileMessageHandler extends SimpleChannelInboundHandler<Command> {

    private Path currentPath = Paths.get("Server","root");
//    private Path currentPath;
    private Path clientPath;
    DBAuthService service = new DBAuthService();


    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.debug("Client connected!");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Command cmd) throws Exception {
//        System.out.println( Files.readString(Paths.get("Server", "log", "lohJson.json")));
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
//                Path fileD = Paths.get(String.valueOf(currentPath), fileN);
                File fileD = new File("Server/root/"+fileN);
                    fileD.delete();
                    System.out.println(fileN+ " deleted..");
                    ctx.writeAndFlush(new ListResponse(currentPath));
                    ctx.writeAndFlush(new PathResponse(currentPath.toString()));
            case AUTH_REQUEST:
                AuthRequest authRequest = (AuthRequest) cmd;
                String login = authRequest.getLogin();
                String password = authRequest.getPassword();
                AuthResponse authResponse = new AuthResponse();
                if (service.findByLogin(login).equals(password)) {
                    authResponse.setAuthStatus(true);
                    clientPath = Paths.get("D:\\GB cloud storage\\Lesson_1\\cloud-storage-sep-2021\\server-sep-2021", login);
                    if (!Files.exists(clientPath)) {
                        Files.createDirectory(clientPath);
                    }
                    currentPath = clientPath;
                } else {
                    authResponse.setAuthStatus(false);
                }
                ctx.writeAndFlush(authResponse);
                break;
//            case AUTH_REQUEST:
//                AuthRequest authRequest = (AuthRequest) cmd;
//                String login = authRequest.getLogin();
//                String password = authRequest.getPassword();
//                String post = authRequest.getPost();
//                AuthResponse authResponse = new AuthResponse();
//                // TODO: 30.10.2021 логиниться буду через Json file(тогда придется поменять немного AUTH)
////                JSONObject jsonObject = new JSONObject(new FileReader(""));
////                System.out.println( Files.readString(Paths.get("Server", "root", "lohJson.json")));
//               File js = new File(Files.readString(Paths.get("Server", "log", "lohJson.json")));
//               JSONObject jsonObject = new JSONObject(new File(Files.readString(Paths.get("Server", "log", "lohJson.json"))));
//               switch (post) {
//                   case "Admin":
//                       if(jsonObject.getJSONObject("arrAdmin").getString("login").equals(login)
//                               & jsonObject.getJSONObject("arrAdmin").getString("password").equals(password)){//тут нужно как-то разделить каждого юзера
//
//                           authResponse.setAuthStatus(true);
//                           clientPath = Paths.get("D:\\GB cloud storage\\Lesson_1\\cloud-storage-sep-2021\\server-sep-2021", login);
//                           if (!Files.exists(clientPath)) {
//                               Files.createDirectory(clientPath);
//                           }
//                           currentPath = clientPath;
//                       } else {
//                           authResponse.setAuthStatus(false);
//
//                           // TODO: 30.10.2021 сделать так чтоб у каждого человека в json была своя папка на серве
//                       }
//                       ctx.writeAndFlush(new AuthResponse());
//                       break;
//                   case "Author":
//                       break;
//                   case "DepartmentEditor":
//                       break;
//                   case "ChiefEditor":
//                       break;
//               }

//                ctx.writeAndFlush(authResponse);
//                break;
            default:
                log.debug("Invalid command {}", cmd.getType());
                break;
        }
    }
}