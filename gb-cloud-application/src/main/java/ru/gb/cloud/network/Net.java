package ru.gb.cloud.network;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import lombok.extern.slf4j.Slf4j;
import ru.gb.cloud.model.AbstractMessage;

import java.io.IOException;
import java.net.Socket;

@Slf4j
public class Net {

    private final Socket socket;

    public ObjectDecoderInputStream getIs() {
        return is;
    }

    private final ObjectDecoderInputStream is;

    public ObjectEncoderOutputStream getOs() {
        return os;
    }

    private final ObjectEncoderOutputStream os;

    private final String host;
    private final int port;

    public Net(String host, int port) throws IOException {
        this.host = host;
        this.port = port;
        socket = new Socket(host, port);
        os = new ObjectEncoderOutputStream(socket.getOutputStream());
        is = new ObjectDecoderInputStream(socket.getInputStream());
    }

    public AbstractMessage read() throws Exception {
        return (AbstractMessage) is.readObject();
    }

    public void write(AbstractMessage message) throws IOException {
        os.writeObject(message);
    }

    public void download() throws IOException {
        os.writeChars("#download");
    }

}
