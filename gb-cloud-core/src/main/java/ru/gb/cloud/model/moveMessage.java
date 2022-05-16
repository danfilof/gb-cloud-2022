package ru.gb.cloud.model;

import lombok.Getter;

@Getter
public class moveMessage extends AbstractMessage{

    public String getMoveData() {
        return moveData;
    }

    private final String moveData;

    public moveMessage(String moveFileData) {
        this.moveData = moveFileData;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.MOVE;
    }
}
