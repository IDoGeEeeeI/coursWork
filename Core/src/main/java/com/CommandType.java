package com;

public enum CommandType {
    FILE_MESSAGE,//Класс-команда для отправки файла c сервера на клиент или с клиента на сервер
    FILE_REQUEST,//Класс-команда для запроса отправки файла с сервера на клиент
    LIST_REQUEST,//Класс-команда для запроса у сервера передачи списка файлов и папок в текущей директории
    LIST_RESPONSE,//Класс-команда для передачи списка файлов и папок в текущей директории сервера
    PATH_RESPONSE,//Класс-команда для установки папки  *
    FILE_DELETED_REQUEST,//Класс-команда для запроса удаления с сервера
    AUTH_REQUEST,//Класс-команда для запроса на вход
    AUTH_RESPONSE//Класс-команда для передачи данных аккаунта пользователя
}
