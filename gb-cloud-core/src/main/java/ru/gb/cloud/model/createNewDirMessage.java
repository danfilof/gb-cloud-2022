package ru.gb.cloud.model;

import lombok.Getter;

@Getter
public class createNewDirMessage extends AbstractMessage{

    public String getNewDir() {
        return newDir;
    }

    private final String newDir;

    public createNewDirMessage(String dir) {
        this.newDir = dir;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.CREATENEWDIR;
    }
}
