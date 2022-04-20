package ru.gb.cloud;

import java.io.*;
import java.lang.reflect.Array;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

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
                String received = is.readUTF();
                System.out.println(received);
                if (received.equals("#addFile#")) {
                    String fileName = is.readUTF();
                    System.out.println("FileName: " + fileName);
                    addFile(fileName);
                    String[] files = dir.list();
                    os.writeUTF("#list#");
                    os.writeLong(files.length);
                    for (String file : files) {
                        os.writeUTF(file);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addFile(String fileName) throws IOException {
        try {
            Path path = Paths.get("ServerFiles");
            Path newFile = Files.createFile(path.resolve(fileName));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
