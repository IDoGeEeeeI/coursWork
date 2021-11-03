package com;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

// TODO: 03.11.2021 !!!!!!!!!!!!Для админа нужно добавить возможность перемещаться между папками сервера 
// TODO: 30.10.2021 короче, я добавлю 4 модуля для каждого актера, а запускать через другой
@Slf4j
public class Controller implements Initializable {

    private static Path currentDir = Paths.get("Client", "root");
    private static Path serverDir;
    public AnchorPane mainScene;

    public TextField loginField;
    public TextField passwordField;
    public Button Authorization;
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
    private URL location;
    @FXML
    private TextArea TextAreaDown;
    @FXML
    private ListView dataClient;
    @FXML
    private ListView dataServer;
    @FXML
    private Stage stage;
    @FXML
    private Scene scene;
    @FXML
    private Parent parent;
    @FXML
    private ListView<String> status1;
    @FXML
    private ListView<String> status2;
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
    private MenuItem help;
    @FXML
    private MenuItem deleteFile;
    @FXML
    private Button DeleteFileBut;

    //методы для переключения сцен(я думаю они не понадобятся)
//    public void switchScene1(ActionEvent actionEvent) throws IOException {
//        parent = FXMLLoader.load(getClass().getResource("Author.fxml"));
//        stage = (Stage) ((Node)actionEvent.getSource()).getScene().getWindow();
//        scene = new Scene(parent);
//        stage.setScene(scene);
//        stage.show();
//    }
//    public void switchScene2(ActionEvent actionEvent) throws IOException {
//        parent = FXMLLoader.load(getClass().getResource("in.fxml"));
//        stage = (Stage) ((Node)actionEvent.getSource()).getScene().getWindow();
//        scene = new Scene(parent);
//        stage.setScene(scene);
//        stage.show();
//    }

    // TODO: 26.10.2021 тут есть методы для логина (нужно будет добавить пару классов в core для этого, т.е. добавить комманды для логина)
    public void sendLoginAndPassword(ActionEvent actionEvent) {
        String login = loginField.getText();
        String password = passwordField.getText();
        loginField.clear();
        passwordField.clear();
        net.sendCommand(new AuthRequest(login, password));
    }

    public void receiveArrayFiles(ActionEvent actionEvent) {
        net.sendCommand(new ListRequest());
    }

    public void updateArrayFiles(ActionEvent actionEvent) throws IOException {
        refreshClientView();
        log.debug("Update Client List");
    }



    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // TODO: 03.11.2021 тут добавить цикл что проверялось есть ли папка юзера, если нет, то создавалась(и все рефреши пихать в него) 
            //тестовая часть для серва(просто быстрее запустить клиент)
//            JSONObject jsonObject = new JSONObject(Files.readString(Paths.get("Client", "root", "lohJson.json")));
//                    if(jsonObject.has("user1")){
//
//                    }else {
//
//                    }

            refreshClientView();
            addNavigationListener();
        } catch (IOException e) {
            e.printStackTrace();
        }
        net = Net.getInstance(cmd -> {
                    switch (cmd.getType()) {
                        case LIST_RESPONSE:
                            ListResponse listResponse = (ListResponse) cmd;
                            refreshServerView(listResponse.getList());
                            break;
                        case FILE_MESSAGE:
                            FileMessage fileMessage = (FileMessage) cmd;
                            Files.write(
                                    currentDir.resolve(fileMessage.getName()),
                                    fileMessage.getBytes()
                            );
                            refreshClientView();
                            break;
                        case PATH_RESPONSE:
                            PathResponse pathResponse = (PathResponse) cmd;
                            System.out.println(pathResponse);
                        case AUTH_RESPONSE:
//                            // TODO: 01.11.2021
                            AuthResponse authResponse = (AuthResponse) cmd;
                            log.debug("AuthResponse {}", authResponse.getAuthStatus());
                            if (authResponse.getAuthStatus()) {
//                                mainScene.setVisible(true);
//                                loginField.setVisible(false);
//                                passwordField.setVisible(false);
//                                Authorization.setVisible(false);
                                // TODO: 03.11.2021  нужно добавить окно логина, и тогда можно будет отключать и включать сцены после логина
                                net.sendCommand(new ListRequest());
                            } else {
                                //todo Warning тут я просто буду выводить в поле (окне логина), что не верный логи/пароль
                            }
//
                            break;
                        default:
                            log.debug("Invalid command {}", cmd.getType());
                    }
                }
        );
    }
    public void deleteFile(){
        // TODO: 30.10.2021 ниже есть код для удаления с сервера, но я думаю, что нужно
        //  сделать отдельную кнопку под него и только для админа или главного

//        DeleteFileBut.setOnMouseClicked(e->{
//            if(e.getClickCount()==1) {
//                String itemS = fileServerView.getSelectionModel().getSelectedItem();
//                net.sendCommand(new FileDeleteRequest(Path.of(itemS)));
////
//            }
//        });

        //удаление с клиента
        DeleteFileBut.setOnMouseClicked(e->{
                String itemC = fileClientView.getSelectionModel().getSelectedItem();
                File file = new File("Client/root/"+itemC);
                if(file.delete()){
                    System.out.println(file+ " deleted..");
                    try {
                        refreshClientView();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
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

    //  todo * и наверное можно дать возможность удалять с серва только админу
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
                TextAreaDown.setText(item);

                //выводить содержимое файла
                try {
                    // TODO: 29.10.2021 решить проблему с русским языком в файлах
                    System.out.println( Files.readString(Paths.get("Client", "root", item)));
                    TextAreaDown.setText(Files.readString(Paths.get("Client", "root", item)));

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });


    }
}