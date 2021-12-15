package com;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
/**
 * Класс контроллер для приложения(и для окна логина/пароля и для основного приложения)
 * Поля:
 * currentDir - путь к рабочей папке
 * Scene - основная сцена
 * sceneLog - сцена при входе
 * sceneMain - сцена рабочей среды
 * net - объект Net, с помощью его реализуем отправку и прием команд
 */
@Slf4j
public class Controller implements Initializable {

    private static Path currentDir;
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
    private Button DeleteFileBut;
    @FXML
    private Button DeleteFileServer;
    @FXML
    private  TextField loginText;
    @FXML
    private  TextField passwordText;
    @FXML
    private  Button upButtonServer;
    @FXML
    private  Button downButtonServer;
    @FXML
    private Button saveText;
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


    /**
     * Инициализация компонентов графического интерфейса.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String currentUsersHomeDir = System.getProperty("user.home");
        String otherFolder = currentUsersHomeDir + File.separator + "Desktop" + File.separator + "workDir";
        File path = new  File(otherFolder);
        if (!path.exists()){
            path.mkdir();
        }
        currentDir = Path.of(otherFolder);
        log.debug(System.getProperty("os.name"));
        try {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    disableScene(sceneMain);
                    enableScene(sceneLog);
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
                            if(listResponse.getStat())
                            net.sendCommand(new UpdateDateFileRequest(true));
                            else net.sendCommand(new UpdateDateFileRequest(false));

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
                                        addUserSplit.setVisible(true);
                                        addUserSplit.setDisable(false);
                                        net.sendCommand(new ListRequest(true));
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
                                        disableButt(loadTo);
                                        net.sendCommand(new ListRequest(true));
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
                                        enableButt(loadTo);
                                        net.sendCommand(new ListRequest(true));
                                    }
                                    case "DepartmentEditor" -> {
                                        enableScene(sceneMain);
                                        disableScene(sceneLog);
                                        enableButt(upButtonServer);
                                        enableButt(downButtonServer);
                                        enableButt(DeleteFileServer);
                                        disableSplitMenuButton(addUserSplit);
                                        disableButt(dellUser);
                                        disableButt(loadTo);
                                        net.sendCommand(new ListRequest(true));
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
    /**
     * Методы для отключения и включения сцен, кнопок.
     */
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
    public void disableSplitMenuButton(SplitMenuButton a){
        a.setDisable(true);
        a.setVisible(false);
    }

    /**
     * Метод для выхода.
     */
    public  void logOut(){
        net.sendCommand(new AuthOutRequest());
    }


    /**
     * Метод для отправки данных входа на сервер
     */
    public void sendLoginAndPassword() {
        String login = loginText.getText();
        String password = passwordText.getText();
        loginText.clear();
        passwordText.clear();
        net.sendCommand(new AuthRequest(login, password));
    }

    /**
     * Метод для обновления view(где показываются файлы в рабочей папке).
     */
    public void updateClient() throws IOException {
        refreshClientView();
        dataClientUpdate();
        log.debug("Update Client List");
    }
    /**
     * Метод для удаления сотрудника.
     */
    public void  dellUser(){
        dellUser.setOnMouseClicked(e->{
            if(e.getClickCount()==2 && !TextAreaDown.getText().isEmpty()){
                String str = TextAreaDown.getText();
                net.sendCommand(new DeleteEmployee(str));
                TextAreaDown.clear();
            }
        });
    }
    /**
     * Метод для добавления сотрудника.
     */
    public   void addUser(){
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
    /**
     * Метод для перемещения по папкам на сервере(вверх).
     */
    public void  upServer() {
        upButtonServer.setOnMouseClicked(e->{
            net.sendCommand(new PathUpRequest());
        });
    }
    /**
     * Метод для перемещения по папкам на сервере(вниз).
     */
    public void  inServer() {
        downButtonServer.setOnMouseClicked(e->{
            String item = fileServerView.getSelectionModel().getSelectedItem();
            net.sendCommand(new PathInRequest(item));
        });
    }
    /**
     * Метод для перемещения по папкам на клиенте(вверх).
     */
    public void clientPathUp() throws IOException {
        if (currentDir.getParent() != null) {
            fileClientView.getItems().clear();
            currentDir = currentDir.getParent();
            refreshClientView();
            dataClientUpdate();
        }
    }
    /**
     * Метод для перемещения по папкам на клиенте(вниз).
     */
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
    /**
     * Метод для перемещения по папкам на клиенте(вверх).
     */
    public void loadFileTo(){
        loadTo.setOnMouseClicked(e->{
            if (e.getClickCount()==1){
                String item = fileClientView.getSelectionModel().getSelectedItem();
                Path newPath = currentDir.resolve(item);
                try {
                    net.sendCommand(new LoadFile(newPath));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
    /**
     * Метод для удаления файла с клиента.
     */
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
    /**
     * Метод для удаления файла с сервера.
     */
    public void deleteFromServer(){
        //удаление с сервера
        DeleteFileServer.setOnMouseClicked(e->{
            if(e.getClickCount()==1) {
                String itemS = fileServerView.getSelectionModel().getSelectedItem();
                net.sendCommand(new FileDeleteRequest(Path.of(itemS)));
            }
        });
    }
    /**
     * Метод для отправки файла на сервер.
     */
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
    /**
     * Метод для скачивания файла.
     */
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
    /**
     * Метод для сохранения изменений файла(для .txt).
     */
    @FXML
    public void saveTextInfile(){
        saveText.setOnMouseClicked(e->{
            if(e.getClickCount()==1){
                String str =  TextAreaDown.getText();//текст файла
                String fileSelected = fileClientView.getSelectionModel().getSelectedItem();//название файла
                for(String str1 : DirORFile()) {
                    if(str1.contains(fileSelected)) {
                        try {
                            String oldFileStr = Files.readString(currentDir.resolve(fileSelected));
                            Files.delete(currentDir.resolve(fileSelected));
                            Files.writeString(currentDir.resolve(fileSelected),oldFileStr + str);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }

                }
            }
        });
    }
    /**
     * Метод для обновления view сервера.
     */
    private void refreshServerView(List<String> names) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                fileServerView.getItems().clear();
                fileServerView.getItems().addAll(names);
            }
        });
    }
    /**
     * Метод для обновления view клиента.
     */
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
    /**
     * Метод для проверки является ли это файлом.
     */
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
    /**
     * Метод для обновления dateView клиента.
     */
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
    /**
     * Метод для обновления dateView сервера.
     */
    private  void dateServerUpdate(List<String> names){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                dataServer.getItems().clear();
                dataServer.getItems().addAll(names);
            }
        });
    }
    /**
     * Методы для определения типа файла.
     */
    private boolean regexMatchesDocx(String str) {
        String pattern = "^[A-Za-z0-9+_.-]+(.docx)$";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(str);
        return m.matches();
    }
    private boolean regexMatchesTxt(String str){
        String pattern = "^[A-Za-z0-9+_.-]+(.txt)$";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(str);
        return m.matches();
    }
//    private boolean regexMatchesRtf(String str){
//        String pattern = "^[A-Za-z0-9+_.-]+(.rtf)$";
//        Pattern r = Pattern.compile(pattern);
//        Matcher m = r.matcher(str);
//        return m.matches();
//    }

    /**
     * Метод слушатель(в нем обновляются view сервера и клиента).
     */
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
                        if(regexMatchesDocx(item)){//word
                            try {
//                                File file = new File("/Users/dmitrijpankratov/Desktop/workDir/rrrr.docx");
                                File file = new File(String.valueOf(currentDir.resolve(item)));
                                FileInputStream fis = new FileInputStream(file);
                                XWPFDocument document = new XWPFDocument(fis);
                                List<XWPFParagraph> paragraphs = document.getParagraphs();
                                for (XWPFParagraph para : paragraphs) {
                                    TextAreaDown.setText(para.getText());
                                }
                                log.debug(String.valueOf(currentDir.resolve(item)));
                                fis.close();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }else if(regexMatchesTxt(item)){//txt
                            //выводить содержимое файла
                            try {
                                log.debug(String.valueOf(currentDir.resolve(item)));
                                TextAreaDown.setText(Files.readString(currentDir.resolve(item), StandardCharsets.UTF_8));
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }else{
                        continue;
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