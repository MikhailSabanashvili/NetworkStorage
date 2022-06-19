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
    private UserService userService;

    public CloudFileHandler() {
        userService = new UserService();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CloudMessage cloudMessage) throws Exception {
        if (cloudMessage instanceof FileRequest fileRequest) {
            ctx.writeAndFlush(new FileMessage(UserService.getLoginDir().get(fileRequest.getLogin()).resolve(fileRequest.getName()), fileRequest.getLogin()));
        } else if (cloudMessage instanceof FileMessage fileMessage) {
            Files.write(UserService.getLoginDir().get(fileMessage.getLogin()).resolve(fileMessage.getName()), fileMessage.getData());
            ListFiles listFiles = new ListFiles(UserService.getLoginDir().get(fileMessage.getLogin()));
            if(!UserService.getLoginDir().get(fileMessage.getLogin()).equals(UserService.getLoginRootDir().get(fileMessage.getLogin()))) {
                listFiles.getFiles().add(0, "..");
            }
            ctx.writeAndFlush(listFiles);
        } else if (cloudMessage instanceof PathInRequest request) {
            String fileName = request.getName();
            Path currentDir = UserService.getLoginDir().get(request.getLogin()).resolve(Path.of(fileName));
            UserService.getLoginDir().remove(request.getLogin());
            UserService.getLoginDir().put(request.getLogin(), currentDir);
            ListFiles list = new ListFiles(currentDir);
            list.getFiles().add(0, "..");
            ctx.writeAndFlush(list);
        } else if(cloudMessage instanceof PathUpRequest request) {
            Path currentDir = UserService.getLoginDir().get(request.getLogin()).getParent();
            UserService.getLoginDir().remove(request.getLogin());
            UserService.getLoginDir().put(request.getLogin(), currentDir);
            ListFiles list = new ListFiles(currentDir);
            if(currentDir.equals(UserService.getLoginRootDir().get(request.getLogin()))) {
                ctx.writeAndFlush(list);
                return;
            }
            list.getFiles().add(0, "..");
            ctx.writeAndFlush(list);
        } else if(cloudMessage instanceof FileDeleteRequest request) {
            String fileName = request.getName();
            Path file = UserService.getLoginDir().get(request.getLogin()).resolve(Path.of(fileName));
            try {
                Files.delete(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ListFiles list = new ListFiles(UserService.getLoginDir().get(request.getLogin()));
            if(!UserService.getLoginDir().get(request.getLogin()).equals(UserService.getLoginRootDir().get(request.getLogin())))
                list.getFiles().add(0, "..");
            ctx.writeAndFlush(list);
        } else if(cloudMessage instanceof AuthorizeRequest request) {
            User user = new User(request.getLogin(),request.getPassword(), request.getFirstName(),
                    request.getSecondName(), request.getEmail());
            userService.authorize(user);
            if(userService.isAuth(user.getLogin(), user.getPassword()))
                ctx.writeAndFlush(new AuthResponse(true, user.getLogin()));
            else
                ctx.writeAndFlush(new AuthResponse(false, user.getLogin()));
        } else if(cloudMessage instanceof AuthRequest request) {
            if(userService.isAuth(request.getLogin(), request.getPassword())) {
                ctx.writeAndFlush(new AuthorizeResponse(true, request.getLogin()));
            }
            else
                ctx.writeAndFlush(new AuthorizeResponse(false, request.getLogin()));
        } else if(cloudMessage instanceof  ListFilesRequest request) {
            ListFiles list = new ListFiles(UserService.getLoginDir().get(request.getLogin()));
            ctx.writeAndFlush(list);
        } else if(cloudMessage instanceof RequestPath path) {
            ctx.writeAndFlush(new ResponsePath(UserService.getLoginDir().get(path.getLogin())));
        } else if(cloudMessage instanceof LoginResponse response) {
            ListFiles list = new ListFiles(UserService.getLoginDir().get(response.getLogin()));
            ctx.writeAndFlush(list);
        }

    }
}
