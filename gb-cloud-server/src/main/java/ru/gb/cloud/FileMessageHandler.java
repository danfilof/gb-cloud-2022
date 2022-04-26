package ru.gb.cloud;

import java.io.*;
import java.net.Socket;

public class FileMessageHandler implements Runnable{

    private final File dir;
    private final DataInputStream is;
    private final DataOutputStream os;




    public FileMessageHandler(Socket socket) throws IOException {
        is = new DataInputStream(socket.getInputStream());
        os = new DataOutputStream(socket.getOutputStream());
        System.out.println("Client accepted");
        dir = new File("ServerFiles");
        String[] files = dir.list();
        os.writeUTF("#list#");
        os.writeLong(files.length);
        for (String file : files) {
            os.writeUTF(file);
        }
    }
    @Override
    public void run() {
        try {
            while (true) {
                String command = is.readUTF();
                if (command.equals("#addFile#")) {
                    readFile();
                    sendStatusOk();
                    reload();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendStatusOk() throws IOException {
        os.writeUTF("#status#");
        os.writeUTF("OK");
        os.flush();
    }

    private void readFile() throws IOException {
        String fileName = is.readUTF();
        File file = dir.toPath().resolve(fileName).toFile();
        long size = is.readLong();

        byte[] buffer = new byte[256];

        try (OutputStream fos = new FileOutputStream(file)){
            for (int i = 0; i < (size + 255) / 256; i++) {
                int readCount = is.read(buffer);
                fos.write(buffer, 0, readCount);
            }
        }
    }

    private void reload() throws Exception{
        String[] files = dir.list();
        os.writeUTF("#list#");
        os.writeLong(files.length);
        for (String file : files) {
            os.writeUTF(file);
        }
    }
}
