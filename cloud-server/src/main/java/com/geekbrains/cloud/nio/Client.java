package com.geekbrains.cloud.nio;

import java.io.IOException;

public class Client {
    public static void main(String[] args) throws IOException {
        NioClient nioClient = new NioClient();
        nioClient.start();
    }
}
