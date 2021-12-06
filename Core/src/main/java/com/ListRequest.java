package com;


public class ListRequest extends Command{
    boolean stat;//1(true) - полная информация, 0(false) - информация только о папке клиента
    public ListRequest(boolean stat){
        this.stat=stat;
    }
    public boolean getStat(){
        return stat;
    }

    @Override
    public CommandType getType() {
        return CommandType.LIST_REQUEST;
    }
}
