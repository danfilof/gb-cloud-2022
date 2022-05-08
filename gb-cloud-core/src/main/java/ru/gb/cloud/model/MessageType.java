package ru.gb.cloud.model;

public enum MessageType {
    FILE("file"),
    LIST("list"),
    DOWNLOAD("download"),
    DELETE("delete"),
    AUTH("auth");



    private final String name;

    public String getName() {
        return name;
    }

    MessageType(String name) {
        this.name = name;
    }
}
