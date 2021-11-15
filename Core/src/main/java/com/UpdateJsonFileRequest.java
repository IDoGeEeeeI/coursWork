package com;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


public class UpdateJsonFileRequest extends Command{


    private String name;
    private final byte[] bytes;
    private String post;
    private String idP;

    public UpdateJsonFileRequest(Path path, String str, String id) throws IOException {
        name = path.getFileName().toString();
        bytes = Files.readAllBytes(path);
        post=str;
        idP=id;
    }

    public  String getName(){return name;}
    public byte[] getBytes() {
        return bytes;
    }
    public String getPost(){return post;}
    public String getId(){return idP;}

    @Override
    public CommandType getType() {
        return CommandType.UPDATE_JSON_FILE_REQUEST;
    }

}
