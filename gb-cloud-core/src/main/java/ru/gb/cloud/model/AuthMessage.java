package ru.gb.cloud.model;

public class AuthMessage extends AbstractMessage {

    public String getAuthData() {
        return authData;
    }

    private final String authData;

    public AuthMessage(String data) {
        this.authData = data;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.AUTH;
    }
}
