package com.geekbrains.cloud.netty.handler;

import com.geekbrains.cloud.CloudMessage;
import com.geekbrains.cloud.FileMessage;
import com.geekbrains.cloud.FileRequest;
import com.geekbrains.cloud.ListFiles;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

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
            /**
             Обработка нажатия на серверной стороне
             Двойной клик на папку -> заходим в папку(возвращаем лист файлов из новой директории)
             Двойной клик на .. -> возврат в родительский каталог
             + ограничение - за server_files нельзя выйти(ибо нефиг гулять по серверу)
             */
            if(fileRequest.isClicked()) {
                String fileName = fileRequest.getName();
                if (fileName.equals("..")) {
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
                currentDir = currentDir.resolve(Path.of(fileName));
                ListFiles list = new ListFiles(currentDir);
                list.getFiles().add(0, "..");
                ctx.writeAndFlush(list);
                return;
            }
            ctx.writeAndFlush(new FileMessage(currentDir.resolve(fileRequest.getName())));
        } else if (cloudMessage instanceof FileMessage fileMessage) {
            Files.write(currentDir.resolve(fileMessage.getName()), fileMessage.getData());
            ListFiles listFiles = new ListFiles(currentDir);
            if(!currentDir.equals(rootDir))
                listFiles.getFiles().add(0, "..");
            ctx.writeAndFlush(listFiles);
        }

    }
}
