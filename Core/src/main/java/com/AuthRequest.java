package com;

public class AuthRequest extends Command {
    public AuthRequest(String login, String password) {
        this.login = login;
        this.password = password;
    }

    private String login;
    private String password;

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