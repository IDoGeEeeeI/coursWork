package com;

public class UpdateDateFileRequest extends  Command{
    @Override
    public CommandType getType() {

        return CommandType.UPDATE_DATE_FILE_REQUEST;
    }
}
