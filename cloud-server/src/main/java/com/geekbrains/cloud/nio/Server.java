package com.geekbrains.cloud.nio;

import java.io.IOException;

public class Server {
    public static void main(String[] args) throws IOException {
        NioServer nioServer = new NioServer();
        nioServer.start();
    }
}
