package com.geekbrains.cloud.june.cloudapplication;

import com.geekbrains.cloud.CloudMessage;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Network {

    private ObjectDecoderInputStream is;

    public ObjectDecoderInputStream getIs() {
        return is;
    }

    public ObjectEncoderOutputStream getOs() {
        return os;
    }

    private ObjectEncoderOutputStream os;

    public Network(int port) throws IOException {
        Socket socket = new Socket("localhost", port);
        os = new ObjectEncoderOutputStream(socket.getOutputStream());
        is = new ObjectDecoderInputStream(socket.getInputStream());
    }

    public String readString() throws IOException {
        return is.readUTF();
    }

    public CloudMessage read() throws IOException, ClassNotFoundException {
        return (CloudMessage) is.readObject();
    }

    public void write(CloudMessage msg) throws IOException {
        os.writeObject(msg);
        os.flush();
    }

    public int readInt() throws IOException {
        return is.readInt();
    }

    public void writeMessage(String message) throws IOException {
        os.writeUTF(message);
        os.flush();
    }

    public String readMessage() throws IOException {
        return readString();
    }
}
