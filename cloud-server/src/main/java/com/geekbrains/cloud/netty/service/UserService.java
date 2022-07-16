package com.geekbrains.cloud.netty.service;

import com.geekbrains.cloud.netty.dao.DaoImpl;
import com.geekbrains.cloud.netty.dao.User;
import com.geekbrains.cloud.netty.exceptions.UnathorizedException;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;

public class UserService {
    private DaoImpl dao;
    @Getter
    private User user;
    @Getter
    private static ConcurrentHashMap<String, Path> loginDir = new ConcurrentHashMap<>();
    @Getter
    private static ConcurrentHashMap<String, Path> loginRootDir = new ConcurrentHashMap<>();

    public UserService() {
        this.dao = DaoImpl.getDao();
    }

    public boolean isAuth(String login, String password) throws UnathorizedException {
        if(isAuthorize(login, password)) {
            createUserDirIfNotExist(login);
            loginRootDir.put(login, Path.of(login));
            loginDir.put(login, Path.of(login));
            return true;
        }
        return false;
    }

    public void authorize(User user) {
        this.user = user;
        dao.insertUser(user.getLogin(), user.getPassword(), user.getFirstName(), user.getSecondName(), user.getEmail());
    }
    
    private void createUserDirIfNotExist(String login) {
        try {
            if(!new File(login).exists())
                Files.createDirectory(Path.of(login));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isAuthorize(String login, String password) throws UnathorizedException {
        this.user = dao.getUser(login);
        if(user == null)
            throw new UnathorizedException();
        return this.user.getPassword().equals(password);
    }
}
