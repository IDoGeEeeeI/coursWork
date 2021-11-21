package com;

import java.util.List;

public class UpdateDateFileResponse extends Command{

    private List<String> res;

    public  UpdateDateFileResponse(List<String> res){
        this.res=res;
    }
    public List<String> getList() {
        return res;
    }

    @Override
    public CommandType getType() {
        return CommandType.UPDATE_DATE_FILE_RESPONSE;
    }

}
