package ru.gb.cloud.nio;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

public class Terminal {

    private Path dir;
    private final ServerSocketChannel serverChannel;
    private final Selector selector;
    private final ByteBuffer buffer = ByteBuffer.allocate(256);

    public Terminal() throws  IOException{

        dir = Path.of("ServerFiles");

        serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(8189));
        serverChannel.configureBlocking(false);

        selector = Selector.open();

        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("Server started on port: 8189");


        while (serverChannel.isOpen()) {
            selector.select();
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectedKeys.iterator();
            try {
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    if (key.isAcceptable()) {
                        handleAccept(key);
                    }
                    if (key.isReadable()) {
                        handleRead(key);
                    }
                    iterator.remove();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        String message = readMessageFromChannel(channel).trim();
        System.out.println("Received: " + message);
        if (message.equals("ls")) {
            channel.write(ByteBuffer.wrap(getLsResultString().getBytes(StandardCharsets.UTF_8)));
        }
        if (message.equals("cat")) {
            channel.write(ByteBuffer.wrap(getCatResultString().getBytes(StandardCharsets.UTF_8)));
        }
        if (message.equals("mkdir")) {
            channel.write(ByteBuffer.wrap(mkdirCommand().getBytes(StandardCharsets.UTF_8)));
        }

        channel.write(ByteBuffer.wrap("-> ".getBytes(StandardCharsets.UTF_8)));
    }

    private String getLsResultString() throws IOException {
        return Files.list(dir).map(p -> p.getFileName()
                .toString()).collect(Collectors
                .joining("\n\r")) + "\n\r";
    }

    private String getCatResultString() throws IOException {
        String catStr = null;
        try {
            RandomAccessFile reader = new RandomAccessFile("ServerFiles/commands_file.txt", "r");
            FileChannel channel = reader.getChannel();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int bufferSize = 256;
            if (bufferSize > channel.size()) {
                bufferSize = (int) channel.size();
            }
            ByteBuffer buff = ByteBuffer.allocate(bufferSize);

            while (channel.read(buff) > 0) {
                out.write(buff.array(), 0, buff.position());
                buff.clear();
            }
            catStr = new String(out.toByteArray(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return catStr;
    }

    private String mkdirCommand() throws IOException {
        String fileName = "ServerFiles/mkdir_test_dir";
        String status = null;

        Path path = Paths.get(fileName);

        if (!Files.exists(path)) {

            Files.createDirectory(path);
            status = "Directory created";
            System.out.println(status);
        } else {
            status = "Directory already exists";
            System.out.println(status);
        }
        return status;
    }


    private String readMessageFromChannel(SocketChannel channel) throws IOException {
        StringBuilder sb = new StringBuilder();
        while (true) {
            int readCount = channel.read(buffer);
            if (readCount == -1) {
                channel.close();
                break;
            }
            if (readCount == 0) {
                break;
            }
            buffer.flip();
            while (buffer.hasRemaining()) {
                sb.append((char)buffer.get());
            }
            buffer.clear();
        }
        return sb.toString();
    }

    private void handleAccept(SelectionKey key) throws IOException {
        SocketChannel channel = serverChannel.accept();
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);
        System.out.println("Client accepted...");
        channel.write(ByteBuffer.wrap("Welcome in terminal!\n\r-> ".getBytes(StandardCharsets.UTF_8)));
    }

    public static void main(String[] args) throws IOException {
        new Terminal();
    }
}
