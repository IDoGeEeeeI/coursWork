package com;
/**
 * Класс-команда для запроса удаления работника из бд.
 * Поля:
 * name - имя(логин пользователя)
 */
public class DeleteEmployee extends  Command {

    private final String name;

    public DeleteEmployee(String string){
        name=string;
    }

    public  String getName(){
        return name;
    }

    @Override
    public CommandType getType() {
        return CommandType.DELETE_EMPLOYEE;
    }
}
