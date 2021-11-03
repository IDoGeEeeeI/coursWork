package com;

public class AuthResponse extends Command{

    private boolean authStatus;
    private String post;

    public boolean getAuthStatus() {
        return authStatus;
    }
    public String getPost(){
        return post;
    }

    public void setAuthStatus(boolean authStatus) {
        this.authStatus = authStatus;
    }
    public void setPost(String post){
        this.post = post;
    }

    @Override
    public CommandType getType() {
        return CommandType.AUTH_RESPONSE;
    }
}