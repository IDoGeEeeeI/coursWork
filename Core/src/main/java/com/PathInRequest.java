package com;

/**
 * Класс-команда для запроса перехода на уровень ниже.
 * Поля:
 * dir - текущая директория
 */
public class PathInRequest extends Command {
    private final String dir;

    public PathInRequest(String dir) {
        this.dir = dir;
    }

    public String getDir() {
        return dir;
    }
    @Override
    public CommandType getType() {
        return CommandType.PATH_IN_REQUEST;
    }
}