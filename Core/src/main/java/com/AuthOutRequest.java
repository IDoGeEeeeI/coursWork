package com;

public class AuthOutRequest extends  Command{
    @Override
    public CommandType getType() {
        return CommandType.AUTH_OUT_REQUEST;
    }
}
