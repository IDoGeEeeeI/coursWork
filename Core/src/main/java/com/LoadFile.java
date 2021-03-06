package com;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
/**
 * Класс-команда для отправки файла, который готов к публикации.
 * Поля:
 * name - название файла
 * bytes - байты файла
 */
public class LoadFile extends  Command{
    private final String name;
    private final byte[] bytes;

    public LoadFile(Path path) throws IOException {
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
        return CommandType.LOAD_FILE;
    }

}
