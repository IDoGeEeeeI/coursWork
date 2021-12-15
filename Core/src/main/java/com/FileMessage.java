package com;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
/**
 * Класс-команда для отправки файла c сервера на клиент или с клиента на сервер.
 * Поля:
 * name - название файла
 * byte - байты файла
 */
public class FileMessage extends Command {

    private final String name;
    private final byte[] bytes;

    public FileMessage(Path path) throws IOException {
        name = path.getFileName().toString();
        bytes = Files.readAllBytes(path);
    }

    public String getName() {
        return name;
    }

    public byte[] getBytes() {
        return bytes;
    }

    @Override
    public CommandType getType() {
        return CommandType.FILE_MESSAGE;
    }

}