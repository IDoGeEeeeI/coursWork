package com;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Класс-команда для передачи списка файлов и папок в текущей директории сервера.
 * Поля:
 * stat - статус для вывода разной информации (полной или не полной)
 * list - список файлов на сервере(файлы клиента)
 * listL - список файлов, которые были одобрены и готовы в публикации.
 * newList - объединенный список файлов(который уже отправляется клиенту)
 */
public class ListResponse extends Command {

    private boolean stat;
    private  List<String> list;
    private  List<String> listL;
    private  List<String> newList;

    public ListResponse(Path path) throws IOException {
        newList = Files.list(path)
                .map(this::resolveFileType)
                .collect(Collectors.toList());
        stat=false;
    }
    public ListResponse(Path path, Path load) throws IOException {
        List<String> VOIDUP = new ArrayList<>(Collections.singletonList("ВЫЛОЖЕННЫЕ ФАЙЛЫ"));
        List<String> VOIDDOWN = new ArrayList<>(Collections.singletonList(""));
        VOIDDOWN.add("ВАШИ ФАЙЛЫ");
        list = Files.list(path)
                .map(this::resolveFileType)
                .collect(Collectors.toList());
        listL = Files.list(load)
                .map(this::resolveFileType)
                .collect(Collectors.toList());
        newList = Stream.of(VOIDUP,listL,VOIDDOWN,list)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        stat=true;

    }

    private String resolveFileType(Path path) {
        return path.getFileName().toString();
    }

    public boolean getStat(){return stat;}
    public List<String> getList() {
        return newList;
    }
    public List<String> getListMain(){
        return list;
    }

    @Override
    public CommandType getType() {
        return CommandType.LIST_RESPONSE;
    }
}