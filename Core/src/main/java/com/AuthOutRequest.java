package com;
/**
 * Класс-команда для запроса аутентификации.
 */
public class AuthOutRequest extends  Command{
    @Override
    public CommandType getType() {
        return CommandType.AUTH_OUT_REQUEST;
    }
}
