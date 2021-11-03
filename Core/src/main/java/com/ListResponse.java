package com;

import java.util.List;
import java.util.stream.Collectors;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


public class ListResponse extends Command {

    private final List<String> list;

    public ListResponse(Path path) throws IOException {
        list = Files.list(path)
                .map(this::resolveFileType)
                .collect(Collectors.toList());
    }

    private String resolveFileType(Path path) {
        return path.getFileName().toString();
    }


    public List<String> getList() {
        return list;
    }

    @Override
    public CommandType getType() {
        return CommandType.LIST_RESPONSE;
    }
}