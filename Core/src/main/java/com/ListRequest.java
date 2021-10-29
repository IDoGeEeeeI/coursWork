package com;


public class ListRequest extends Command{
    @Override
    public CommandType getType() {

        return CommandType.LIST_REQUEST;
    }
}
