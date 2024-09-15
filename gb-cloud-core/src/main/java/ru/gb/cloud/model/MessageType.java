package ru.gb.cloud.model;

public enum MessageType {
    FILE("file"),
    LIST("list"),
    DOWNLOAD("download"),
    DELETE("delete"),
    AUTH("auth"),
    CHANGEFILENAME("change file name"),
    DIRECT("directory"),
    CREATENEWDIR("create new dir"),

    MOVE("move");



    private final String name;

    public String getName() {
        return name;
    }

    MessageType(String name) {
        this.name = name;
    }
}
