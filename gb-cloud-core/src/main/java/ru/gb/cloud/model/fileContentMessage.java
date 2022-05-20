package ru.gb.cloud.model;

import lombok.Getter;

@Getter
public class fileContentMessage extends AbstractMessage{

    public String getFileText() {
        return fileText;
    }

    private final String fileText;

    public fileContentMessage(String fileContent) {
        this.fileText = fileContent;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.fileContent;
    }
}
