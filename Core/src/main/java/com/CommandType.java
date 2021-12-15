package com;
/**
 * Enum для команд.
 */
public enum CommandType {
    FILE_MESSAGE,//Класс-команда для отправки файла c сервера на клиент или с клиента на сервер
    FILE_REQUEST,//Класс-команда для запроса отправки файла с сервера на клиент
    LIST_REQUEST,//Класс-команда для запроса у сервера передачи списка файлов и папок в текущей директории
    LIST_RESPONSE,//Класс-команда для передачи списка файлов и папок в текущей директории сервера
    PATH_RESPONSE,//Класс-команда для установки папки  *
    FILE_DELETED_REQUEST,//Класс-команда для запроса удаления с сервера
    AUTH_REQUEST,//Класс-команда для запроса на вход
    AUTH_RESPONSE,//Класс-команда для передачи данных аккаунта пользователя
    PATH_UP_REQUEST,//Класс-команда для запроса на переход на уровень выше
    PATH_IN_REQUEST,//Класс-команда для запроса на уровень ниже
    AUTH_OUT_RESPONSE,//Класс-команда для отправки статуса аутентификации
    AUTH_OUT_REQUEST,//Класс-команда для запроса аутентификации
    UPDATE_DATE_FILE_REQUEST,//Класс-команда для запроса даты изменения файлов
    UPDATE_DATE_FILE_RESPONSE,//Класс-команда для отправки даты изменения файлов
    UPDATE_JSON_FILE_REQUEST,//Класс-команда для добавления работника в файл, хранящего информацию о работниках
    DELETE_EMPLOYEE,//Класс-команда для запроса удаления работника из бд
    LOAD_FILE,//Класс-команда для отправки файла, который готов к публикации
}
