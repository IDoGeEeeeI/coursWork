package com;
/**
 * Класс-команда для запроса на вход.
 * Поля:
 * login - логин;
 * password - пароль;
 */

public class AuthRequest extends Command {
    public AuthRequest(String login, String password) {
        this.login = login;
        this.password = password;
    }

    private final String login;
    private final String password;

    public String getLogin() {
        return login;
    }
    public String getPassword() {
        return password;
    }

    @Override
    public CommandType getType() {
        return CommandType.AUTH_REQUEST;
    }
}