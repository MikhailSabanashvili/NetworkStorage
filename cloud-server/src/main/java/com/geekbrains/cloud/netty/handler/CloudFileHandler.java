package com.geekbrains.cloud.netty.handler;

import com.geekbrains.cloud.*;
import com.geekbrains.cloud.netty.dao.User;
import com.geekbrains.cloud.netty.exceptions.UnathorizedException;
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
            String login = userService.getUser().getLogin();
            ctx.writeAndFlush(new FileMessage(UserService.getLoginDir().get(login).resolve(fileRequest.getName())));
        } else if (cloudMessage instanceof FileMessage fileMessage) {
            String login = userService.getUser().getLogin();
            Files.write(UserService.getLoginDir().get(login).resolve(fileMessage.getName()), fileMessage.getData());
            ListFiles listFiles = new ListFiles(UserService.getLoginDir().get(login));
            if(!UserService.getLoginDir().get(login).equals(UserService.getLoginRootDir().get(login))) {
                listFiles.getFiles().add(0, "..");
            }
            ctx.writeAndFlush(listFiles);
        } else if (cloudMessage instanceof PathInRequest request) {
            String fileName = request.getName();
            String login = userService.getUser().getLogin();
            Path currentDir = UserService.getLoginDir().get(login).resolve(Path.of(fileName));
            UserService.getLoginDir().remove(login);
            UserService.getLoginDir().put(login, currentDir);
            ListFiles list = new ListFiles(currentDir);
            list.getFiles().add(0, "..");
            ctx.writeAndFlush(list);
        } else if(cloudMessage instanceof PathUpRequest request) {
            String login = userService.getUser().getLogin();
            Path currentDir = UserService.getLoginDir().get(login).getParent();
            UserService.getLoginDir().remove(login);
            UserService.getLoginDir().put(login, currentDir);
            ListFiles list = new ListFiles(currentDir);
            if(currentDir.equals(UserService.getLoginRootDir().get(login))) {
                ctx.writeAndFlush(list);
                return;
            }
            list.getFiles().add(0, "..");
            ctx.writeAndFlush(list);
        } else if(cloudMessage instanceof FileDeleteRequest request) {
            String fileName = request.getName();
            String login = userService.getUser().getLogin();
            Path file = UserService.getLoginDir().get(login).resolve(Path.of(fileName));
            try {
                Files.delete(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ListFiles list = new ListFiles(UserService.getLoginDir().get(login));
            if(!UserService.getLoginDir().get(login).equals(UserService.getLoginRootDir().get(login)))
                list.getFiles().add(0, "..");
            ctx.writeAndFlush(list);
        } else if(cloudMessage instanceof AuthorizeRequest request) {
            User user = new User(request.getLogin(),request.getPassword(), request.getFirstName(),
                    request.getSecondName(), request.getEmail());
            userService.authorize(user);
            try {
                userService.isAuth(user.getLogin(), user.getPassword());
                ctx.writeAndFlush(new AuthorizeResponse(true));
            } catch (UnathorizedException e) {
                ctx.writeAndFlush(new AuthorizeResponse(false));
            }
        } else if(cloudMessage instanceof AuthRequest request) {
            try {
                userService.isAuth(request.getLogin(), request.getPassword());
                ctx.writeAndFlush(new AuthResponse(true));
            } catch (UnathorizedException e) {
                ctx.writeAndFlush(new AuthResponse(false));
            }
        } else if(cloudMessage instanceof  ListFilesRequest) {
            ListFiles list = new ListFiles(UserService.getLoginDir().get(userService.getUser().getLogin()));
            ctx.writeAndFlush(list);
        }

    }
}
