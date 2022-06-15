package com.geekbrains.cloud.netty.handler;

import com.geekbrains.cloud.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.file.Files;
import java.nio.file.Path;

public class CloudFileHandler extends SimpleChannelInboundHandler<CloudMessage> {

    private Path currentDir;
    private Path rootDir;

    public CloudFileHandler() {
        currentDir = Path.of("server_files");
        rootDir = currentDir;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(new ListFiles(currentDir));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CloudMessage cloudMessage) throws Exception {
        if (cloudMessage instanceof FileRequest fileRequest) {
            ctx.writeAndFlush(new FileMessage(currentDir.resolve(fileRequest.getName())));
        } else if (cloudMessage instanceof FileMessage fileMessage) {
            Files.write(currentDir.resolve(fileMessage.getName()), fileMessage.getData());
            ListFiles listFiles = new ListFiles(currentDir);
            if(!currentDir.equals(rootDir))
                listFiles.getFiles().add(0, "..");
            ctx.writeAndFlush(listFiles);
        } else if (cloudMessage instanceof PathInRequest request) {
            String fileName = request.getName();
            currentDir = currentDir.resolve(Path.of(fileName));
            ListFiles list = new ListFiles(currentDir);
            list.getFiles().add(0, "..");
            ctx.writeAndFlush(list);
            return;
        } else if(cloudMessage instanceof PathUpRequest) {
            currentDir = currentDir.getParent();
            ListFiles list = new ListFiles(currentDir);
            if(currentDir.equals(rootDir)) {
                ctx.writeAndFlush(list);
                return;
            }
            list.getFiles().add(0, "..");
            ctx.writeAndFlush(list);
            return;
        }

    }
}
