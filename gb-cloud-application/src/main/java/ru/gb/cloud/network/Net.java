package ru.gb.cloud.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Net {

    private final Socket socket;

    public DataInputStream getIs() {
        return is;
    }

    private final DataInputStream is;

    public DataOutputStream getOs() {
        return os;
    }

    private final DataOutputStream os;

    private final String host;
    private final int port;

    public Net(String host, int port) throws IOException {
        this.host = host;
        this.port = port;
        socket = new Socket(host, port);
        is = new DataInputStream(socket.getInputStream());
        os = new DataOutputStream(socket.getOutputStream());
    }

    public Long readLong() throws IOException {
        return is.readLong();
    }

    public String readUtf() throws IOException {
        return is.readUTF();
    }
}
