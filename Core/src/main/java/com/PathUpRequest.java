package com;
/**
 * Класс-команда для запроса на переход на уровень выше.
 */
public class PathUpRequest extends Command{
    @Override
    public CommandType getType() {
        return CommandType.PATH_UP_REQUEST;
    }
}