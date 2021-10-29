package com;


import java.nio.file.Path;

public class FileRequest extends Command {
    private final String name;

    public FileRequest(Path path) {
        name = path.getFileName().toString();
    }

    public String getName() {
        return name;
    }

    @Override
    public CommandType getType() {
        return CommandType.FILE_REQUEST;
    }
}
