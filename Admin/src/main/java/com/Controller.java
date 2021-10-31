package com;

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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;


// TODO: 30.10.2021 короче, я добавлю 4 модуля для каждого актера, а запускать через другой
@Slf4j
public class Controller implements Initializable {

    private static Path currentDir = Paths.get("Admin", "root");
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




    // TODO: 26.10.2021 тут есть методы для логина (нужно будет добавить пару классов в core для этого, т.е. добавить комманды для логина)
//    public void sendLoginAndPassword(ActionEvent actionEvent) {
//        String login = loginField.getText();
//        String password = passwordField.getText();
//        loginField.clear();
//        passwordField.clear();
//        net.sendCommand(new AuthRequest(login, password));
//    }

    public void receiveArrayFiles(ActionEvent actionEvent) {
        net.sendCommand(new ListRequest());
    }

    public void updateArrayFiles(ActionEvent actionEvent) throws IOException {
        refreshClientView();
    }



    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
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
//                        case AUTH_RESPONSE:
//                            AuthResponse authResponse = (AuthResponse) cmd;
//                            log.debug("AuthResponse {}", authResponse.getAuthStatus());
//                            if (authResponse.getAuthStatus()) {
//                                mainScene.setVisible(true);
//                                loginField.setVisible(false);
//                                passwordField.setVisible(false);
//                                Authorization.setVisible(false);
//                                net.sendCommand(new ListRequest());
//                            } else {
//                                //todo Warning
//                            }
//
//                            break;
//                        default:
//                            log.debug("Invalid command {}", cmd.getType());
                    }
                }
        );
    }
    public void deleteFile(){
        // TODO: 30.10.2021 ниже есть код для удаления с сервера, но я думаю, что нужно
        //  сделать отдельную кнопку под него

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
    // TODO: 30.10.2021 это наверное можно и не исправлять, (я думаю, что проблема с последовательностью действий)
    //  тип когда скачиваешь на клиент вылазит exception,
    //  НО ВСЕ ПРАВИЛЬНО СКАЧИВАЕТСЯ И ОТОБРАЖАЕТСЯ, А ПРИЛОЖЕНИЕ НЕ ПАДАЕТ
    //  * и наверное можно дать возможность удалять с серва только админу
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
    
    // TODO: 30.10.2021 при отправке и скачке ошибку направляют сюда!!!!но прилож не падает
    private void refreshServerView(List<String> names) {
        fileServerView.getItems().clear();
        fileServerView.getItems().addAll(names);
    }

    private void refreshClientView() throws IOException {
        fileClientView.getItems().clear();
        List<String> names = Files.list(currentDir)
                .map(p->p.getFileName().toString())
                .collect(Collectors.toList());
        fileClientView.getItems().addAll(names);
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
                    //todo когда буду делать регистрацию нужно папку рут делать для каждого

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