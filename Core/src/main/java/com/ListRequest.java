package com;

/**
 * Класс-команда для запроса у сервера передачи списка файлов и папок в текущей директории.
 * Поля:
 * stat - статус, из-за которого может отправляться различные данные(полный список или не полный)
 */
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
