package com.geekbrains.cloud.nio;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class NioServer {
    private ServerSocketChannel server;
    private Selector selector;

    public NioServer() throws IOException {
        server = ServerSocketChannel.open();
        selector = Selector.open();
        server.bind(new InetSocketAddress(8189));
        server.configureBlocking(false);
        server.register(selector, SelectionKey.OP_ACCEPT);
    }

    public void start() throws IOException {
        while (server.isOpen()) {
            selector.select();
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                if (key.isAcceptable()) {
                    handleAccept();
                }
                if (key.isReadable()) {
                    handleRead(key);
                }
                iterator.remove();
            }

        }
    }

    private void handleRead(SelectionKey key) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(1024);
        SocketChannel channel = (SocketChannel) key.channel();
        StringBuilder s = new StringBuilder();
        while (channel.isOpen()) {
            int read = channel.read(buf);
            if (read < 0) {
                channel.close();
                return;
            }
            if (read == 0) {
                break;
            }
            buf.flip();
            while (buf.hasRemaining()) {
                s.append((char) buf.get());
            }
            buf.clear();
        }

        String command = s.toString();
        if(command.equals("ls")) {
            doLs(channel);
        } else if(command.contains("cat")) {

        } else if(command.contains("cd")) {

        }


    }

    private void doLs(SocketChannel channel) {
        File dir = new File(System.getProperty("user.home"));
        File[] arrFiles = dir.listFiles();
        assert arrFiles != null;
        List<File> list = Arrays.asList(arrFiles);
        List<String> fileNames = list.stream().map(File::getName).toList();
        fileNames.forEach(x -> writeToClient(x + "\n", channel));
    }

    private void doCat() {

    }

    private void doCd() {

    }

    private void writeToClient(String result, SocketChannel channel) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(result.getBytes(StandardCharsets.UTF_8));
        try {
            channel.write(byteBuffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleAccept() throws IOException {
        SocketChannel channel = server.accept();
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);
        channel.write(ByteBuffer.wrap("Welcome in Mike terminal!\n-> ".getBytes(StandardCharsets.UTF_8)));
    }
}
