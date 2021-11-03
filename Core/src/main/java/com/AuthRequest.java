package com;

public class AuthRequest extends Command {
    public AuthRequest(String login, String password) {
        this.login = login;
        this.password = password;
    }

    private String login;
    private String password;
    private String post;// TODO: 03.11.2021 нужно подумать что запихать в json нужна ли должность или нет   

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }
    public String getPost(){
        return post;
    }

    @Override
    public CommandType getType() {
        return CommandType.AUTH_REQUEST;
    }
}