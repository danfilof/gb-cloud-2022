package ru.gb.cloud.model;

import lombok.Getter;

@Getter
public class DownloadMessage extends AbstractMessage{
    public String getDownloadFileName() {
        return downloadFileName;
    }

    private final String downloadFileName;

    public DownloadMessage(String downloadFile) {
       downloadFileName = downloadFile;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.DOWNLOAD;
    }

}
