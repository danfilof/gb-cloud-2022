package ru.gb.cloud.model;

import lombok.Getter;

@Getter
public class directoryMessage extends AbstractMessage{

    public String getDirectString() {
        return directString;
    }

    private final String directString;

    public directoryMessage(String toOpen) {
        this.directString = toOpen;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.DIRECT;
    }
}
