package com;

import java.io.File;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
/**
 * Клас обработчик сервера(обрабатывает приходящие команды).
 * Поля:
 * serverPathForAdmin - полный путь к папке сервера(для админа, из-за этого он может перемещаться по директориям сервера без преград)
 * serverPathForEmp - путь папке сервера(но не абсолютный)
 * currentPath - пут к текущий папка на сервере
 * logPath - пут к папке в которой хранится json файл
 * clientPath - путь к папке пользователя
 * logFilePath - путь к файлу хранящего данные о пользователях
 * LoadFilesPath - путь к папке, хранящей файлы готовые к публикации
 */
@Slf4j
public class FileMessageHandler extends SimpleChannelInboundHandler<Command> {

    private static Path serverPathForAdmin;
    private Path serverPathForEmp;
    private Path currentPath;
    private Path logPath;
    private Path clientPath ;
    private static Path logFilePath;
    private static  Path LoadFilesPath;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.debug("Client connected!");
        log.debug(System.getProperty("os.name"));
        String currentUsersHomeDir = System.getProperty("user.home");
        String otherFolder = currentUsersHomeDir + File.separator + "Desktop" + File.separator + "serverDir";
        File path = new  File(otherFolder);
        if (!path.exists()){
            path.mkdir();
        }
        if(path.exists() && path.isDirectory()) {
            String otherFolderROOT = currentUsersHomeDir + File.separator + "Desktop" + File.separator + "serverDir" + File.separator + "root";
            File pathROOT = new File(otherFolderROOT);
            if (!pathROOT.exists()) {
                pathROOT.mkdir();
            }
            String otherFolderLOAD = currentUsersHomeDir + File.separator + "Desktop" + File.separator + "serverDir" + File.separator + "root" + File.separator + "LoadFiles";
            File pathLoad = new File(otherFolderLOAD);
            if (!pathLoad.exists()) {
                pathLoad.mkdir();
            }
            LoadFilesPath = Path.of(otherFolderLOAD);
            serverPathForEmp = Path.of(otherFolderROOT);
            currentPath = Path.of(otherFolderROOT);
            String otherFolderLOG = currentUsersHomeDir + File.separator + "Desktop" + File.separator + "serverDir" + File.separator + "log";
            File pathLOG = new File(otherFolderLOG);
            if (!pathLOG.exists()) {
                pathLOG.mkdir();
            }
            String otherFileJSON = currentUsersHomeDir + File.separator + "Desktop" + File.separator + "serverDir" + File.separator + "log" + File.separator + "lohJson.json";
//            try {
//                String file = Files.readString(
//                        Path.of(currentUsersHomeDir + File.separator + "Desktop" + File.separator + "serverDir" + File.separator + "log" + File.separator + "lohJson.json")
//                );
//                if(Files.size( Path.of(currentUsersHomeDir + File.separator + "Desktop" + File.separator + "serverDir"
//                        + File.separator + "log" + File.separator + "lohJson.json"))==0){
//                    otherFileJSON = currentUsersHomeDir + File.separator + "Desktop" + File.separator + "serverDir" + File.separator + "log" + File.separator + "lohJson.json";
////                /Users/dmitrijpankratov/Desktop/coursework/Server/rootServ/log/defLog.json
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            logFilePath = Path.of(otherFileJSON);
            logPath = Path.of(otherFolderLOG);
            serverPathForAdmin = Path.of(otherFolder);
        }else{
            log.debug("ERROR add rootDir");
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Command cmd) throws Exception {
        log.debug("Received command from client: {}", cmd.getType());
        switch (cmd.getType()) {
            case LOAD_FILE ->{
                LoadFile loadFile = (LoadFile) cmd;
                Files.write(LoadFilesPath.resolve(loadFile.getName()),
                        loadFile.getBytes());
            }
            case FILE_MESSAGE -> {
                FileMessage fileMessage = (FileMessage) cmd;
                Files.write(
                        currentPath.resolve(fileMessage.getName()),
                        fileMessage.getBytes()
                );
                ctx.writeAndFlush(new ListResponse(currentPath,LoadFilesPath));
                log.debug("Received a file {} from the client", fileMessage.getName());
            }
            case FILE_REQUEST -> {
                FileRequest fileRequest = (FileRequest) cmd;
                String fileName = fileRequest.getName();
                Path file = Paths.get(String.valueOf(currentPath), fileName);// тут ошибка когда пытаешься скачать файл,
                // который выложили(хз нужно ли давать возможность качать файлы, которые выложили)
                if(Files.exists(file) && !Files.isDirectory(file)){
                    if(!fileName.equals("ВАШИ ФАЙЛЫ") && !fileName.equals("ВЫЛОЖЕННЫЕ ФАЙЛЫ")){
                        ctx.writeAndFlush(new FileMessage(file));
                        log.debug("Send file {} to the client", fileName);
                    }
                }
            }
            case LIST_REQUEST -> {
                ListRequest listRequest = (ListRequest) cmd;
                if(listRequest.getStat()){
                    ctx.writeAndFlush(new ListResponse(currentPath,LoadFilesPath));
                }else {
                    ctx.writeAndFlush(new ListResponse(currentPath));
                }
                ctx.writeAndFlush(new PathResponse(currentPath.toString()));
                log.debug("Send list of files to the client");
            }
            case FILE_DELETED_REQUEST -> {
                FileDeleteRequest fileDeleteRequest = (FileDeleteRequest) cmd;
                String fileN = fileDeleteRequest.getName();
                File fileD = new File(String.valueOf(currentPath.resolve(fileN)));
                fileD.delete();
                System.out.println(fileN + " deleted..");
                ctx.writeAndFlush(new ListResponse(currentPath,LoadFilesPath));
                ctx.writeAndFlush(new PathResponse(currentPath.toString()));
            }
            case AUTH_REQUEST -> {
                AuthRequest authRequest = (AuthRequest) cmd;
                String login = authRequest.getLogin();
                String password = authRequest.getPassword();
                JSONObject jsonObject = new JSONObject(Files.readString(logFilePath));
                String post;
                String main1,main2="";
                AuthResponse authResponse = new AuthResponse();
                if (jsonObject.has(login)) {
                    if (jsonObject.getJSONObject(login).getString("Password").equals(password)) {
                        post = jsonObject.getJSONObject(login).getString("Post");
                        main1 =jsonObject.getJSONObject(login).getString("Main");//должность вышестоящего
                        //main2 - должность вышестоящего у main1(для автора это главный редакторы)
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
                                clientPath = serverPathForAdmin;
                                if (!Files.exists(clientPath)) {
                                    Files.createDirectory(clientPath);
                                }
                                currentPath = clientPath;
                            }
                            case "ChiefEditor"->{
                                clientPath= serverPathForEmp.resolve(login);
                                if (!Files.exists(clientPath)) {
                                    Files.createDirectory(clientPath);
                                }
                                currentPath = clientPath;
                            }
                            case "DepartmentEditor"->{
                                clientPath = serverPathForEmp.resolve(main1+"/"+login);
                                if(!Files.exists(serverPathForEmp.resolve(main1))){
                                    Files.createDirectory(serverPathForEmp.resolve(main1));
                                }
                                if (!Files.exists(clientPath)) {
                                    Files.createDirectory(clientPath);
                                }
                                currentPath = clientPath;
                            }
                            case "Author" ->{
                                clientPath = serverPathForEmp.resolve(main2+"/"+main1+"/"+login);
                                if(!Files.exists(serverPathForEmp.resolve(main2+"/"+main1))){
                                    Files.createDirectory(serverPathForEmp.resolve(main2+"/"+main1));
                                }
                                if(!Files.exists(serverPathForEmp.resolve(main1))){
                                    Files.createDirectory(serverPathForEmp.resolve(main1));
                                }
                                if (!Files.exists(clientPath)) {
                                    Files.createDirectory(clientPath);
                                }
                                currentPath = clientPath;
                            }
                        }
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
                    ctx.writeAndFlush(new ListResponse(currentPath,LoadFilesPath));
                    ctx.writeAndFlush(new PathResponse(currentPath.toString()));
                } else {
                    log.debug("{} is not a directory",request);
                }
            }
            case PATH_UP_REQUEST -> {
                if (currentPath.getParent() != null) {
                    if (currentPath.equals(clientPath)) {//блок по правам
                        log.debug("Above the client's folder , it is not necessary to rise");
                    } else {
                        currentPath = currentPath.getParent();
                    }
                }
                log.debug("Send list of files and current directory to the client");
                ctx.writeAndFlush(new ListResponse(currentPath,LoadFilesPath));
                ctx.writeAndFlush(new PathResponse(currentPath.toString()));
            }
            case AUTH_OUT_REQUEST->{
                AuthOutResponse authOutResponse = new AuthOutResponse();
                authOutResponse.setAuthOutStatus(false);
                ctx.writeAndFlush(new AuthOutResponse());
            }
            case UPDATE_DATE_FILE_REQUEST -> {
                UpdateDateFileRequest updateDateFileRequest = (UpdateDateFileRequest) cmd;
                if(updateDateFileRequest.getStat()) {
                    List<String> results = new ArrayList<>();
                    File[] fileLoads = new File(String.valueOf(LoadFilesPath)).listFiles();
                    File[] files = new File(String.valueOf(currentPath)).listFiles();
                    assert files != null;
                    results.add("\n");
                    for (File file : fileLoads) {
                        SimpleDateFormat sdf1 = new SimpleDateFormat("dd.MM.yyyy 'в' HH:mm:ss");
                        String format = sdf1.format(file.lastModified());
                        results.add(format);
                    }
                    results.add("\n");
                    results.add("\n");
                    for (File file : files) {
                        SimpleDateFormat sdf1 = new SimpleDateFormat("dd.MM.yyyy 'в' HH:mm:ss");
                        String format = sdf1.format(file.lastModified());
                        results.add(format);
                    }
                    ctx.writeAndFlush(new UpdateDateFileResponse(results));
                }else {
                    List<String> results = new ArrayList<>();
                    File[] files = new File(String.valueOf(currentPath)).listFiles();
                    assert files != null;
                    for (File file : files) {
                        SimpleDateFormat sdf1 = new SimpleDateFormat("dd.MM.yyyy 'в' HH:mm:ss");
                        String format = sdf1.format(file.lastModified());
                        results.add(format);
                    }
                    ctx.writeAndFlush(new UpdateDateFileResponse(results));
                }
            }
            case UPDATE_JSON_FILE_REQUEST -> {
                UpdateJsonFileRequest updateJsonFileRequest = (UpdateJsonFileRequest) cmd;
                String postAndId = updateJsonFileRequest.getPost()+updateJsonFileRequest.getId();
                JSONObject json = new JSONObject(Files.readString(logFilePath));
                Files.write(
                        logPath.resolve(updateJsonFileRequest.getName()),
                        updateJsonFileRequest.getBytes()
                );
                JSONObject jsonAppend = new JSONObject(
                    Files.readString(
                            Paths.get(String.valueOf(logPath.resolve(updateJsonFileRequest.getName())))
                    )
                );
                        JSONObject dataInf = new JSONObject();
                        dataInf.put("surname",jsonAppend.getJSONObject(postAndId).getJSONObject("data").getString("surname"));
                        dataInf.put("name",jsonAppend.getJSONObject(postAndId).getJSONObject("data").getString("name"));
                        dataInf.put("gender",jsonAppend.getJSONObject(postAndId).getJSONObject("data").getString("gender"));
                    JSONObject employeeDetails = new JSONObject();
                    employeeDetails.put("Password", jsonAppend.getJSONObject(postAndId).getString("Password"));
                    employeeDetails.put("Post", jsonAppend.getJSONObject(postAndId).getString("Post"));
                    employeeDetails.put("Main", jsonAppend.getJSONObject(postAndId).getString("Main"));
                    employeeDetails.put("data", dataInf);
                json.put(postAndId,employeeDetails);
                Files.delete(logFilePath);
                Files.writeString(
                        logFilePath,
                        json.toString()
//                        gson.toJson(json)//пока что не разобрался с gson(есть кое-какие проблемы)
                );
                ctx.writeAndFlush(new ListResponse(logPath,LoadFilesPath));
                ctx.writeAndFlush(new PathResponse(logPath.toString()));
                Files.delete(logPath.resolve(updateJsonFileRequest.getName()));
                log.debug("Send log list of files to the client");
            }
            case DELETE_EMPLOYEE -> {
                DeleteEmployee deleteEmployee = (DeleteEmployee) cmd;
                JSONObject json = new JSONObject(
                        Files.readString(logFilePath)
                );
                json.remove(deleteEmployee.getName());
                ctx.writeAndFlush(new ListResponse(logPath,LoadFilesPath));
                ctx.writeAndFlush(new PathResponse(logPath.toString()));
                log.debug("Send log list of files to the client");
                Files.delete(logFilePath);
                Files.writeString(
                        logFilePath,
                        json.toString()
                );
            }
            default -> log.debug("Invalid command {}", cmd.getType());
        }
    }
}