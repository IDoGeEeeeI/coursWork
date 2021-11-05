package com;

public class AuthOutResponse extends  Command{
    private boolean authOutStatus;

    public boolean getAuthOutStatus() {
        return authOutStatus;
    }
    public void setAuthOutStatus(boolean authOutStatus) {
        this.authOutStatus = authOutStatus;
    }

    @Override
    public CommandType getType() {
        return CommandType.AUTH_OUT_RESPONSE;
    }
}
