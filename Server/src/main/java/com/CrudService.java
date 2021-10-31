package com;

import java.util.List;

public interface CrudService<T, ID> {
    T save(T object);

    T remove(T object);

    T removeById(ID id);

    T findById(ID id);

    List<T> findAll();

}