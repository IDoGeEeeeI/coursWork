package com;

public class UpdateDateFileRequest extends  Command{
    private boolean stat;//1(true) - полная информация, 0(false) - информация только о папке клиента
    public UpdateDateFileRequest(boolean stat){
        this.stat=stat;
    }
    public boolean getStat(){
        return stat;
    }
    @Override
    public CommandType getType() {

        return CommandType.UPDATE_DATE_FILE_REQUEST;
    }
}
