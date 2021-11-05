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
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
// TODO: 04.11.2021 реализовать само приложение, где будут публикации

// TODO: 04.11.2021 ****************** можно еще добавить отмену последнего действия(например, отмена удаления)

@Slf4j
public class Controller implements Initializable {

    private static Path currentDir = Paths.get("Client", "root");
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
    private URL location;
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
                                        net.sendCommand(new ListRequest());
                                        String a; // просто чтоб не светилось
                                        // TODO: 04.11.2021 реализовать удаление
                                        //у него будет еще поле, где он может выбирать кому из  сотрудников отправить
                                    }
                                    case "DepartmentEditor" -> {//удаление только у ниже стоящих (авторы)
                                        turnOnScene(sceneMain);
                                        disableScene(sceneLog);
                                        net.sendCommand(new ListRequest());
                                        int v;// просто чтоб не светилось
                                        // TODO: 04.11.2021
                                        //у него будет еще поле, где он может выбирать кому из  сотрудников отправить
                                    }
                                    default -> {
                                        // TODO: 04.11.2021 когда буду делать реализацию обычного пользователя, то нужно case дописать
                                    }
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

    public void updateServer(ActionEvent actionEvent) {
        net.sendCommand(new ListRequest());
    }

    public void updateClient(ActionEvent actionEvent) throws IOException {
        refreshClientView();
        log.debug("Update Client List");
    }

    public void  upServer() {
        upButtonServer.setOnMouseClicked(e->{
            net.sendCommand(new PathUpRequest());
        });
    }
    public void  inServer() {
        upButtonServer.setOnMouseClicked(e->{
            String item = fileServerView.getSelectionModel().getSelectedItem();
            net.sendCommand(new PathInRequest(item));
        });
    }
    public void clientPathUp(ActionEvent actionEvent) throws IOException {
        if (currentDir.getParent() != null) {
            fileClientView.getItems().clear();
            currentDir = currentDir.getParent();
            refreshClientView();
        }
    }
    public void  clientPathIn(ActionEvent actionEvent){

        String item = fileClientView.getSelectionModel().getSelectedItem();
        currentDir = currentDir.resolve(item);
        try {
            refreshClientView();
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
//                Path item = Path.of(fileClientView.getSelectionModel().getSelectedItem());

                if (!Files.isDirectory(Path.of(item))) {
                    TextAreaDown.setText(String.valueOf(item));
                    //выводить содержимое файла
                    try {
                        // TODO: 29.10.2021 решить проблему с русским языком в файлах
                        // TODO: 04.11.2021 короче, при вставке файла кодировка другая, а при вставке текста в файл - все норм
                        //  *upd ворд файл не открывается совсем
                        //  наверное будет проще реализовать написание текстов в самой проге
                        //  можно добавит еще https://javadevblog.com/chtenie-dokumenta-word-v-formate-docx-s-pomoshh-yu-apache-poi.html

//standardCharsets.UTF-8
                        log.debug(String.valueOf(currentDir.resolve(item)));
                        TextAreaDown.setText(Files.readString(currentDir.resolve(item)));// TODO: 05.11.2021 проблема в том что он dir распознает как file
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }else {
                    TextAreaDown.setText(item);
                    log.debug(String.valueOf(currentDir.resolve(item)));
                }
            }
        });
    }
}