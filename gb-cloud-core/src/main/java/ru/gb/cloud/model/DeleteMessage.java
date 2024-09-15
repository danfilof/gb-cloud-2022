package ru.gb.cloud.model;

import lombok.Getter;

@Getter
public class DeleteMessage extends AbstractMessage {

    public String getDeleteFileName() {
        return deleteFileName;
    }

    private final String deleteFileName;

    public DeleteMessage(String deleteFile) {
        this.deleteFileName = deleteFile;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.DELETE;
    }
}
