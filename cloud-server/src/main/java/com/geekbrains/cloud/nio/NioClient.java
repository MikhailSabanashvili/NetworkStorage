package com.geekbrains.cloud.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class NioClient {
    private SocketChannel client;
    private Selector selector;

    public NioClient() throws IOException {
        client = SocketChannel.open(new InetSocketAddress(8189));
        selector = Selector.open();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ);
        Thread thread = new Thread(() -> {
            while(true) {
                read();
            }
        });
        thread.start();
    }

    public void start() throws IOException {
        while(client.isOpen()) {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String command = scanner.next();
                client.write(ByteBuffer.wrap(command.getBytes(StandardCharsets.UTF_8)));
            }

        }
    }

    private void read() {
        try {
            selector.select();
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                if (key.isReadable()) {
                    handleRead(key);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void handleRead(SelectionKey key) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(1024);
        SocketChannel channel = (SocketChannel) key.channel();
        StringBuilder s = new StringBuilder();
        List<String> list = new ArrayList<>();
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
            list.add(String.valueOf(s));
            buf.clear();
        }

        list.forEach(System.out::println);
    }

}
