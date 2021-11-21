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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

//  можно добавит еще https://javadevblog.com/chtenie-dokumenta-word-v-formate-docx-s-pomoshh-yu-apache-poi.html
//  ****************** после входа, или до можно выводить окно, где будешь устанавливать root папку


@Slf4j
public class Controller implements Initializable {

    private static Path currentDir = Paths.get("C:\\Users\\Дмитрий\\Desktop\\coursWork-main\\Client", "root");

    public AnchorPane Scene;
    public AnchorPane sceneLog;
    public  AnchorPane sceneMain;
    private Net net;

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
    private Button saveText;//todo
    @FXML
    private Button cleanText;
    @FXML
    private Button loadTo;
    @FXML
    private CheckMenuItem AdminSplit;
    @FXML
    private CheckMenuItem AuthorSplit;
    @FXML
    private CheckMenuItem ChiefEditorSplit;
    @FXML
    private CheckMenuItem DepartmentEditorSplit;
    @FXML
    private SplitMenuButton addUserSplit;
    @FXML
    private TextField idArea;
    @FXML
    private Button dellUser;
    @FXML
    private Menu helpMenu;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    disableScene(sceneMain);
                    enableScene(sceneLog);
                }
            });
            if(Files.exists(currentDir)){
                System.out.println("trueAd");
            }
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
                                        enableScene(sceneMain);
                                        disableScene(sceneLog);
                                        enableButt(upButtonServer);
                                        enableButt(downButtonServer);
                                        enableButt(DeleteFileServer);
                                        enableMenu();
                                        net.sendCommand(new ListRequest());
                                    }
                                    case "Author" -> {
                                        enableScene(sceneMain);
                                        disableButt(DeleteFileServer);
                                        disableButt(upButtonServer);
                                        disableButt(downButtonServer);
                                        disableScene(sceneLog);
                                        enableButt(DeleteFileServer);
                                        disableSplitMenuButton(addUserSplit);
                                        disableButt(dellUser);
                                        net.sendCommand(new ListRequest());
                                    }
                                    case "ChiefEditor" -> {
                                        enableScene(sceneMain);
                                        disableScene(sceneLog);
                                        enableButt(upButtonServer);
                                        enableButt(downButtonServer);
                                        enableButt(DeleteFileServer);
                                        enableButt(loadTo);
                                        disableSplitMenuButton(addUserSplit);
                                        disableButt(dellUser);
                                        enableButt(loadTo);//кнопка для заливания на "сайт"(ничего пока что не делает,
                                        // но будет наверное отправлять в папку для "сайта")
                                        net.sendCommand(new ListRequest());
                                    }
                                    case "DepartmentEditor" -> {
                                        enableScene(sceneMain);
                                        disableScene(sceneLog);
                                        enableButt(upButtonServer);
                                        enableButt(downButtonServer);
                                        enableButt(DeleteFileServer);
                                        disableSplitMenuButton(addUserSplit);
                                        disableButt(dellUser);
                                        net.sendCommand(new ListRequest());

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
                            enableScene(sceneLog);
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
    public void enableScene(AnchorPane a){
        a.setDisable(false);
        a.setVisible(true);
    }
    public void disableButt(Button a){
        a.setDisable(true);
        a.setVisible(false);
    }
    public void enableButt(Button a){
        a.setDisable(false);
        a.setVisible(true);
    }
    public void disableCheckMenuItem(CheckMenuItem a){
        a.setDisable(true);
    }
    public void enableCheckMenuItem(CheckMenuItem a){
        a.setDisable(false);
    }
    public void disableSplitMenuButton(SplitMenuButton a){
        a.setDisable(true);
        a.setVisible(false);
    }
    public void enableSplitMenuButton(SplitMenuButton a){
        a.setDisable(false);
        a.setVisible(true);
    }
    public void disableCheckItems(CheckMenuItem a, CheckMenuItem b, CheckMenuItem c){
        a.setDisable(true);
        a.setVisible(false);
        b.setDisable(true);
        c.setDisable(true);
    }
    public void enableMenu(){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                helpMenu.setDisable(false);
                helpMenu.setVisible(true);
            }
        });
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
    public void  dellUser(ActionEvent actionEvent){
        dellUser.setOnMouseClicked(e->{
            if(e.getClickCount()==2 && !TextAreaDown.getText().isEmpty()){
                String str = TextAreaDown.getText();
                net.sendCommand(new DeleteEmployee(str));
                TextAreaDown.clear();
            }
        });
    }
    public  void addUser(ActionEvent actionEvent){
        addUserSplit.setOnMouseClicked(e->{
            if(e.getClickCount()==1 && AdminSplit.isSelected() && !idArea.getText().isEmpty()){
                String str = AdminSplit.getText();
                String id = idArea.getText();
                String text1 = TextAreaDown.getText();
                try {
                    File newFile = File.createTempFile("text", ".json", null);
                    Files.writeString(Paths.get(String.valueOf(newFile)), text1, StandardOpenOption.APPEND);
                    net.sendCommand(new UpdateJsonFileRequest(Paths.get(String.valueOf(newFile)),str,id));
                    newFile.deleteOnExit();
                    idArea.clear();
                    TextAreaDown.clear();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }else if(e.getClickCount()==1 && ChiefEditorSplit.isSelected() && !idArea.getText().isEmpty()){
                String str = ChiefEditorSplit.getText();
                String id = idArea.getText();
                String text1 = TextAreaDown.getText();
                try {
                    File newFile = File.createTempFile("text", ".json", null);
                    Files.writeString(Paths.get(String.valueOf(newFile)), text1, StandardOpenOption.APPEND);
                    net.sendCommand(new UpdateJsonFileRequest(Paths.get(String.valueOf(newFile)),str,id));
                    newFile.deleteOnExit();
                    idArea.clear();
                    TextAreaDown.clear();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }else if(e.getClickCount()==1 && DepartmentEditorSplit.isSelected() && !idArea.getText().isEmpty()){
                String str = DepartmentEditorSplit.getText();
                String id = idArea.getText();
                String text1 = TextAreaDown.getText();
                try {
                    File newFile = File.createTempFile("text", ".json", null);
                    Files.writeString(Paths.get(String.valueOf(newFile)), text1, StandardOpenOption.APPEND);
                    net.sendCommand(new UpdateJsonFileRequest(Paths.get(String.valueOf(newFile)),str,id));
                    newFile.deleteOnExit();
                    idArea.clear();
                    TextAreaDown.clear();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }else if(e.getClickCount()==1 && AuthorSplit.isSelected() && !idArea.getText().isEmpty()){
                String str = AuthorSplit.getText();
                String id = idArea.getText();
                String text1 = TextAreaDown.getText();
                try {
                    File newFile = File.createTempFile("text", ".json", null);
                    Files.writeString(Paths.get(String.valueOf(newFile)), text1, StandardOpenOption.APPEND);
                    net.sendCommand(new UpdateJsonFileRequest(Paths.get(String.valueOf(newFile)),str,id));
                    newFile.deleteOnExit();
                    idArea.clear();
                    TextAreaDown.clear();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }
    public void  upServer() {
        upButtonServer.setOnMouseClicked(e->{
            net.sendCommand(new PathUpRequest());
        });
    }
    public void  inServer() {
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
                            // TODO: 29.10.2021 решить проблему с русским языком в файлах UPD наверное пофиксил, но проверить (может это проблема была только на маке?!)
                            log.debug(String.valueOf(currentDir.resolve(item)));
                            TextAreaDown.setText(Files.readString(currentDir.resolve(item), StandardCharsets.UTF_8));//по сути я тут и ничего и не менял, там и так в дефолте ютф8
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }

                }
            }
        });
        cleanText.setOnMouseClicked(e->{
            if(e.getClickCount()==1){
                TextAreaDown.clear();
            }
        });
    }


}