package com.geekbrains.cloud.netty.handler;

import com.geekbrains.cloud.*;
import com.geekbrains.cloud.netty.dao.User;
import com.geekbrains.cloud.netty.service.UserService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CloudFileHandler extends SimpleChannelInboundHandler<CloudMessage> {

    private Path currentDir;
    private Path rootDir;
    private UserService userService;

    public CloudFileHandler() {
        currentDir = Path.of("server_files");
        rootDir = currentDir;
        userService = new UserService();
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
        } else if(cloudMessage instanceof PathUpRequest) {
            currentDir = currentDir.getParent();
            ListFiles list = new ListFiles(currentDir);
            if(currentDir.equals(rootDir)) {
                ctx.writeAndFlush(list);
                return;
            }
            list.getFiles().add(0, "..");
            ctx.writeAndFlush(list);
        } else if(cloudMessage instanceof FileDeleteRequest request) {
            String fileName = request.getName();
            Path file = currentDir.resolve(Path.of(fileName));
            try {
                Files.delete(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ListFiles list = new ListFiles(currentDir);
            if(!currentDir.equals(rootDir))
                list.getFiles().add(0, "..");
            ctx.writeAndFlush(list);
        } else if(cloudMessage instanceof AuthorizeRequest request) {
            User user = new User(request.getLogin(),request.getPassword(), request.getFirstName(),
                    request.getSecondName(), request.getEmail());
            userService.authorize(user);
            if(userService.isAuth(user.getLogin(), user.getPassword()))
                ctx.writeAndFlush(new AuthResponse(true));
            else
                ctx.writeAndFlush(new AuthResponse(false));
        } else if(cloudMessage instanceof AuthRequest request) {
            if(userService.isAuth(request.getLogin(), request.getPassword()))
                ctx.writeAndFlush(new AuthorizeResponse(true));
            else
                ctx.writeAndFlush(new AuthorizeResponse(false));
        }

    }
}
