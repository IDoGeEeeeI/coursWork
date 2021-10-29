package com;

import javafx.application.Platform;
import javafx.collections.ObservableList;
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

import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;


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
    private javafx.scene.control.ScrollBar ScrollBar;
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
    private CheckBox fff;
    @FXML
    private TextField test;



    public void switchScene1(ActionEvent actionEvent) throws IOException {
        parent = FXMLLoader.load(getClass().getResource("Client1.fxml"));
        stage = (Stage) ((Node)actionEvent.getSource()).getScene().getWindow();
        scene = new Scene(parent);
        stage.setScene(scene);
        stage.show();
    }
    public void switchScene2(ActionEvent actionEvent) throws IOException {
        parent = FXMLLoader.load(getClass().getResource("in.fxml"));
        stage = (Stage) ((Node)actionEvent.getSource()).getScene().getWindow();
        scene = new Scene(parent);
        stage.setScene(scene);
        stage.show();
    }

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


//    public void sendFile(javafx.event.ActionEvent actionEvent) throws IOException {
//        fileServerView.setOnMouseClicked(e->{
//            if(e.getClickCount()==1){
//                String item = returnName2(fileClientView.getSelectionModel().getSelectedItem());
//                Path newPath = currentDir.resolve(item);
//                net.sendCommand(new FileMessage());
//            }
//        });
//    }
//    public void download(javafx.event.ActionEvent actionEvent) throws IOException {
//        String fileName = listView1.getSelectionModel().getSelectedItem();
//        net.sendCommand(new FileRequest(fileName));
//    }

    public String returnName1(String str) {
        String[] words = str.split(" ");
        return words[0];
    }

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
        fileClientView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String item = fileClientView.getSelectionModel().getSelectedItem();
                Path newPath = currentDir.resolve(item);
                if (Files.isDirectory(newPath)) {
                    currentDir = newPath;

                    try {
                        refreshClientView();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            }
        });
        fileServerView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String item = fileServerView.getSelectionModel().getSelectedItem();
                if (returnName1(fileServerView.getSelectionModel().getSelectedItem()).equals("[Dir]")) {
                    net.sendCommand(new PathInRequest(item));
                }
            }
        });
        fileClientView.setOnMouseClicked(e->{
            if (e.getClickCount() == 1) {
                String item = fileClientView.getSelectionModel().getSelectedItem();
                TextAreaDown.setText(item);

                //выводить содержимое файла
                try {
                    //todo когда буду делать реистрацию нужно пакпку рут делать для каждого
                    System.out.println( Files.readString(Paths.get("Client", "root", item)));
                    TextAreaDown.setText(Files.readString(Paths.get("Client", "root", item)));

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                //


                    // TODO: 28.10.2021  добавить еще окна(для других пользователей, регистрацию)


            }
        });

    }
}