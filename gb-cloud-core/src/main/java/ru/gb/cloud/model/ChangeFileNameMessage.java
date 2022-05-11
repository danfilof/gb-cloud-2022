package ru.gb.cloud.model;

public class ChangeFileNameMessage extends AbstractMessage {

    public String getFileNames() {
        return fileNames;
    }

    private final String fileNames;

    public ChangeFileNameMessage(String originalAndNewName) {
        this.fileNames = originalAndNewName;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.CHANGEFILENAME;
    }
}
