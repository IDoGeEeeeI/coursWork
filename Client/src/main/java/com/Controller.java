package com;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
// TODO: 04.11.2021 реализовать само приложение, где будут публикации


//  ****************** добалю, когда все сделаю(для последующих версий программ )
//  можно добавит еще https://javadevblog.com/chtenie-dokumenta-word-v-formate-docx-s-pomoshh-yu-apache-poi.html
//  ****************** после входа, или до можно выводить окно, где будешь устанавливать root папку
//  ****************** можно еще добавить отмену последнего действия(например, отмена удаления)

@Slf4j
public class Controller implements Initializable {

    private static Path currentDir = Paths.get("C:\\Users\\Дмитрий\\Desktop\\coursWork-main\\Client", "root");//тут можно потом просто поставить папку на рабочем столе
    public AnchorPane Scene;
    public AnchorPane sceneLog;
    public  AnchorPane sceneMain;
    private Net net;
    private final Date date = new Date();

    @FXML
    public ListView<String> fileClientView;
    @FXML
    public ListView<String> fileServerView;
    @FXML
    private Button Upload;
    @FXML
    private Button Download;
    @FXML
    private TextArea TextAreaDown;
    @FXML
    private ListView<String> dataClient;// TODO: 04.11.2021 дату еще не реализовал
    @FXML
    private ListView<String> dataServer;
    @FXML
    private MenuBar menuBar;
    @FXML
    private Menu menuFile;
    @FXML
    private Menu menuEdit;
    @FXML
    private Menu menuHelp;
    @FXML
    private MenuItem backLogin;
    @FXML
    private MenuItem updateServerMenu;
    @FXML
    private MenuItem updateClientMenu;
    @FXML
    private MenuItem deleteFile;
    @FXML
    private Button DeleteFileBut;
    @FXML
    private Button DeleteFileServer;
    @FXML
    private  TextField loginText;
    @FXML
    private  TextField passwordText;
    @FXML
    private  Button buttIN;
    @FXML
    private  Button upButtonClient;
    @FXML
    private  Button downButtonClient;
    @FXML
    private  Button upButtonServer;
    @FXML
    private  Button downButtonServer;
    @FXML
    private TextArea textAreaForClient;
//    @FXML
//    private Button saveTextButton;
    @FXML
    private Button saveText;
    @FXML
    private Button cleanText;



    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    disableScene(sceneMain);
                    turnOnScene(sceneLog);
                }
            });
            refreshClientView();
            dataUpdate();
            addNavigationListener();
        } catch (IOException e) {
            e.printStackTrace();
        }
        net = Net.getInstance(cmd -> {
                    switch (cmd.getType()) {
                        case LIST_RESPONSE -> {
                            ListResponse listResponse = (ListResponse) cmd;
                            refreshServerView(listResponse.getList());
                        }
                        case FILE_MESSAGE -> {
                            FileMessage fileMessage = (FileMessage) cmd;
                            Files.write(
                                    currentDir.resolve(fileMessage.getName()),
                                    fileMessage.getBytes()
                            );
                            refreshClientView();
                        }
                        case PATH_RESPONSE -> {
                            PathResponse pathResponse = (PathResponse) cmd;
                            log.debug(String.valueOf(pathResponse));
                        }
                        case AUTH_RESPONSE -> {
                            AuthResponse authResponse = (AuthResponse) cmd;
                            log.debug("AuthResponse {}", authResponse.getAuthStatus());
                            if (authResponse.getAuthStatus()) {
                                switch (authResponse.getPost()) {
                                    case "Admin" -> {//полный доступ
                                        turnOnScene(sceneMain);
                                        disableScene(sceneLog);
                                        net.sendCommand(new ListRequest());
                                    }
                                    case "Author" -> {//без удаления с сервера, доступ к папке редактора отдела(чтоб ему отправлять)
                                        turnOnScene(sceneMain);
                                        disableButt(DeleteFileServer);
                                        disableButt(upButtonServer);
                                        disableButt(downButtonServer);
                                        disableScene(sceneLog);
                                        net.sendCommand(new ListRequest());
                                    }
                                    case "ChiefEditor" -> {//удаление только у ниже стоящих (автор, редактор отдела)
                                        turnOnScene(sceneMain);
                                        disableScene(sceneLog);
                                        disableButt(upButtonServer);
                                        disableButt(downButtonServer);
                                        net.sendCommand(new ListRequest());
                                        String a; // просто чтоб не светилось
                                        // TODO: 04.11.2021 UPD реализовать кнопку, которая будет удалять файл с серва и отправлять обратно автору
                                    }
                                    case "DepartmentEditor" -> {//удаление только у ниже стоящих (авторы)
                                        turnOnScene(sceneMain);
                                        disableScene(sceneLog);
                                        disableButt(upButtonServer);
                                        disableButt(downButtonServer);
                                        net.sendCommand(new ListRequest());
                                        int v;// просто чтоб не светилось
                                        // TODO: 04.11.2021 todo UPD кнопку для заливания на "сайт"
                                    }
                                    default -> log.debug("Invalid authCommand {}", cmd.getType());
                                }
                            } else {
                                loginText.setText("неверный пароль и логин");
                                loginText.setOnMouseClicked(e -> loginText.selectAll());
                            }
                        }
                        case AUTH_OUT_RESPONSE -> {
                            AuthOutResponse authOutResponse = new AuthOutResponse();
                            log.debug("AuthResponse {}", authOutResponse.getAuthOutStatus());
                            disableScene(sceneMain);
                            turnOnScene(sceneLog);
                        }
                        default -> log.debug("Invalid command {}", cmd.getType());
                    }
                }
        );
    }
    public void disableScene(AnchorPane a){
        a.setDisable(true);
        a.setVisible(false);
    }
    public void turnOnScene(AnchorPane a){
        a.setDisable(false);
        a.setVisible(true);
    }
    public void disableButt(Button a){
        a.setDisable(true);
        a.setVisible(false);
    }
    public void turnOnButt(Button a){
        a.setDisable(false);
        a.setVisible(true);
    }
    public  void logOut(ActionEvent actionEvent){
        net.sendCommand(new AuthOutRequest());
    }

    public void sendLoginAndPassword(ActionEvent actionEvent) {
        String login = loginText.getText();
        String password = passwordText.getText();
        loginText.clear();
        passwordText.clear();
        net.sendCommand(new AuthRequest(login, password));
    }

    public void updateServer() {
        net.sendCommand(new ListRequest());
    }

    public void updateClient() throws IOException {
        refreshClientView();
        dataUpdate();
        log.debug("Update Client List");
    }
    public void saveButtJSON(){//для админа
// TODO: 06.11.2021 boolean createNewFile(): создает новый файл по пути, который передан в конструктор.
//  В случае удачного создания возвращает true, иначе false
       String write = TextAreaDown.getText();
        File newFile = new File(String.valueOf(currentDir),write);
        try
        {
            boolean created = newFile.createNewFile();
            if(created)
                log.debug("File has been created");
        }
        catch(IOException ex){
            log.debug(ex.getMessage());
        }
    }


    public  void  addUser(){

    }

    public void  upServer() {//только для админа
        upButtonServer.setOnMouseClicked(e->{
            net.sendCommand(new PathUpRequest());
        });
    }
    public void  inServer() {//только для админа
        upButtonServer.setOnMouseClicked(e->{
            String item = fileServerView.getSelectionModel().getSelectedItem();
            net.sendCommand(new PathInRequest(item));
        });
    }
    public void clientPathUp() throws IOException {
        if (currentDir.getParent() != null) {
            fileClientView.getItems().clear();
            currentDir = currentDir.getParent();
            refreshClientView();
            dataUpdate();
        }
    }
    public void  clientPathIn(){

        String item = fileClientView.getSelectionModel().getSelectedItem();
        currentDir = currentDir.resolve(item);
        try {
            refreshClientView();
            dataUpdate();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void deleteFile(){
        //удаление с клиента
        DeleteFileBut.setOnMouseClicked(e->{
                String itemC = fileClientView.getSelectionModel().getSelectedItem();
                File file = new File(String.valueOf(currentDir.resolve(itemC)));
                if(file.delete()){
                    log.debug(file+ " deleted..");
                    try {
                        refreshClientView();
                        dataUpdate();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            });
    }
    public void deleteFromServer(){
        //удаление с сервера
        DeleteFileServer.setOnMouseClicked(e->{
            if(e.getClickCount()==1) {
                String itemS = fileServerView.getSelectionModel().getSelectedItem();
                net.sendCommand(new FileDeleteRequest(Path.of(itemS)));
            }
        });
    }

    public void sendFile(){
        Upload.setOnMouseClicked(e->{
            if (e.getClickCount()==1){
                String item = fileClientView.getSelectionModel().getSelectedItem();
                Path newPath = currentDir.resolve(item);
                try {
                    net.sendCommand(new FileMessage(newPath));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
    public void download(){
        Download.setOnMouseClicked(e->{
            if (e.getClickCount()==1){
                String item = fileServerView.getSelectionModel().getSelectedItem();
                net.sendCommand(new FileRequest(Path.of(item)));

                try {
                    refreshClientView();
                    dataUpdate();
                } catch (IOException ex) {
                   ex.printStackTrace();
                }
            }
        });
    }
    private void refreshServerView(List<String> names) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                fileServerView.getItems().clear();
                fileServerView.getItems().addAll(names);
            }
        });
    }
    private void refreshClientView() throws IOException {
        Platform.runLater(new Runnable() {
              @Override
              public void run() {
                  fileClientView.getItems().clear();
                  List<String> names = null;
                  try {
                      names = Files.list(currentDir)
                              .map(p->p.getFileName().toString())
                              .collect(Collectors.toList());
                  } catch (IOException e) {
                      e.printStackTrace();
                  }
                  fileClientView.getItems().addAll(names);
              }


        });
    }
    private List<String> DirORFile(){//выводит только файлы(нужен для того, чтоб правильно выводить в поле)
        List<String> results = new ArrayList<>();
        File[] files = new File(String.valueOf(currentDir)).listFiles();
        for (File file : files) {
            if (file.isFile()) {
                results.add(file.getName());
            }
        }
        return  results;
    }
    // TODO: 06.11.2021    long lastModified(): возвращает время последнего изменения файла или каталога.
    //  Значение представляет количество миллисекунд, прошедших с начала эпохи Unix

    private void dataUpdate(){// TODO: 06.11.2021 я устал, короче нужно фиксить, выводит больше времени чем нужно + время не правильное, нужно еще сделать только для файлов, для папок время не нужно 
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                dataClient.getItems().clear();
                List<String> results = new ArrayList<>();
                File[] files = new File(String.valueOf(currentDir)).listFiles();
                for (File file : files) {
                    if (file.isFile()) {
                        long time;

                time = file.lastModified();
                Date date1 = new java.util.Date(time*1000L);
                SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("HH:mm:ss");
                String format = sdf1.format(date1);
                results.add(format);


//                        results.add(String.valueOf(file.lastModified()));
                    }
                    dataClient.getItems().addAll(results);
                }
//                long sunriseHour;
//                sunriseHour = jsonObject.getJSONObject("sys").getLong("sunrise");
//                // convert seconds to milliseconds
//                Date date1 = new java.util.Date(sunriseHour*1000L);
//                // the format of your date
//                SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("HH:mm:ss");
//                // give a timezone reference for formatting (see comment at the bottom)
////                                    sdf1.setTimeZone(java.util.TimeZone.getTimeZone("GMT-3"));
//                String formattedDateSUNRISE = sdf1.format(date1);
                }
        });
    }

    public void updateDateClient(ActionEvent actionEvent) {
        dataUpdate();
        log.debug("Update Date Client List");
    }
    public void addNavigationListener() {
        fileServerView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1) {
                TextAreaDown.setText("скачайте для просмотра..");
            }
        });
        //пред показ файла
        fileClientView.setOnMouseClicked(e->{
            if (e.getClickCount() == 1) {
                String item = fileClientView.getSelectionModel().getSelectedItem();
                for(String str : DirORFile()) {
                    if(str.contains(item)) {
                        //выводить содержимое файла
                        try {
                            // TODO: 29.10.2021 решить проблему с русским языком в файлах
//standardCharsets.UTF-8
                            log.debug(String.valueOf(currentDir.resolve(item)));
                            TextAreaDown.setText(Files.readString(currentDir.resolve(item)));
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }

                }
            }
        });
        cleanText.setOnMouseClicked(e->{
            if(e.getClickCount()==1){
                TextAreaDown.setText("");
            }
        });
    }


}