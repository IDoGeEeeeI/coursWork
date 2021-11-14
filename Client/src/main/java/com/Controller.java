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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
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
    // (сделать метод, который при запуске программы проверяет есть ли папка и создает если нужно)
    public AnchorPane Scene;
    public AnchorPane sceneLog;
    public  AnchorPane sceneMain;
    private Net net;

    private SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yyyy 'в' HH:mm:ss");
    private Date date = new Date();
//System.out.println(sdf.format(date));

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
    private ListView<String> dataClient;
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
            dataClientUpdate();

            addNavigationListener();
        } catch (IOException e) {
            e.printStackTrace();
        }
        net = Net.getInstance(cmd -> {
                    switch (cmd.getType()) {
                        case LIST_RESPONSE -> {
                            ListResponse listResponse = (ListResponse) cmd;
                            refreshServerView(listResponse.getList());
                            net.sendCommand(new UpdateDateFileRequest());
                        }
                        case FILE_MESSAGE -> {
                            FileMessage fileMessage = (FileMessage) cmd;
                            Files.write(
                                    currentDir.resolve(fileMessage.getName()),
                                    fileMessage.getBytes()
                            );
                            dataClientUpdate();
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
                                    case "Author" -> {
                                        turnOnScene(sceneMain);
                                        disableButt(DeleteFileServer);
                                        disableButt(upButtonServer);
                                        disableButt(downButtonServer);
                                        disableScene(sceneLog);
                                        net.sendCommand(new ListRequest());
                                    }
                                    case "ChiefEditor" -> {
                                        turnOnScene(sceneMain);
                                        disableScene(sceneLog);
//                                        disableButt(upButtonServer);
//                                        disableButt(downButtonServer);
                                        net.sendCommand(new ListRequest());
                                    // TODO: 04.11.2021 todo UPD кнопку для заливания на "сайт" (кнопка сохранить)
                                        String a; // просто чтоб не светилось
                                    }
                                    case "DepartmentEditor" -> {
                                        turnOnScene(sceneMain);
                                        disableScene(sceneLog);
//                                        disableButt(upButtonServer);
//                                        disableButt(downButtonServer);
                                        net.sendCommand(new ListRequest());
                                        int v;// просто чтоб не светилось
                                        // TODO: 04.11.2021 UPD реализовать кнопку, которая будет удалять файл с серва и отправлять обратно автору

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
                        case UPDATE_DATE_FILE_RESPONSE->{
                            UpdateDateFileResponse updateDateFileResponse = (UpdateDateFileResponse) cmd;
                            dateServerUpdate(updateDateFileResponse.getList());
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
        dataClientUpdate();
        log.debug("Update Client List");
    }
    public void saveButtJSON(){//для админа(для добавления клиентов)
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
        //System.out.println( Files.readString(Paths.get("Client", "root", "lohJson.json")));//чтение json
    }

    public  void  addUser(){
    }



    public void  upServer() {//только для админа(может и для главы)
        upButtonServer.setOnMouseClicked(e->{
            net.sendCommand(new PathUpRequest());
        });
    }
    public void  inServer() {//только для админа
        downButtonServer.setOnMouseClicked(e->{
            String item = fileServerView.getSelectionModel().getSelectedItem();
            net.sendCommand(new PathInRequest(item));
        });
    }
    public void clientPathUp() throws IOException {
        if (currentDir.getParent() != null) {
            fileClientView.getItems().clear();
            currentDir = currentDir.getParent();
            refreshClientView();
            dataClientUpdate();
        }
    }
    public void  clientPathIn(){

        String item = fileClientView.getSelectionModel().getSelectedItem();
        currentDir = currentDir.resolve(item);
        try {
            refreshClientView();
            dataClientUpdate();
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
                        dataClientUpdate();
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
                    dataClientUpdate();
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

    private void dataClientUpdate(){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                dataClient.getItems().clear();
                List<String> results = new ArrayList<>();
                File[] files = new File(String.valueOf(currentDir)).listFiles();
                    for (File file : files) {
                    SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("dd.MM.yyyy 'в' HH:mm:ss");
                    String format = sdf1.format(file.lastModified());
//                    String format = "Обновлен "+sdf1.format(file.lastModified());
                    results.add(format);
                    }
                dataClient.getItems().addAll(results);
                }
        });
    }
    private  void dateServerUpdate(List<String> names){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                dataServer.getItems().clear();
                dataServer.getItems().addAll(names);
            }
        });
    }

    public void updateDateClient(ActionEvent actionEvent) {//для кнопок(если будут нужны)
        dataClientUpdate();
        log.debug("Update Date Client List");
    }
    public  void updateDateServer(ActionEvent actionEvent){
        net.sendCommand(new UpdateDateFileRequest());
        log.debug("Update Date Server List");
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