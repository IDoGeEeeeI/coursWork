package com;

public interface AuthService<T> extends CrudService<T, Long> {
    String findByLogin(String login);
}