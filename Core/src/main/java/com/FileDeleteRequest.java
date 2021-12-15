package com;

import java.nio.file.Path;
/**
 * Класс-команда для запроса удаления с сервера.
 * Поля:
 * name - название файла
 */
public class FileDeleteRequest extends  Command{
    private final String name;

    public FileDeleteRequest(Path path) {
        name = path.getFileName().toString();
    }
    public String getName() {
        return name;
    }
    @Override
    public CommandType getType() {
        return CommandType.FILE_DELETED_REQUEST;
    }

}
