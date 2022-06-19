package com.geekbrains.cloud.netty.service;

import com.geekbrains.cloud.netty.dao.DaoImpl;
import com.geekbrains.cloud.netty.dao.User;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;

public class UserService {
    private DaoImpl dao;
    @Getter
    private static ConcurrentHashMap<String, Path> loginDir;
    @Getter
    private static ConcurrentHashMap<String, Path> loginRootDir;

    public UserService() {
        this.dao = new DaoImpl();
        loginDir = new ConcurrentHashMap<>();
        loginRootDir = new ConcurrentHashMap<>();
    }

    public boolean isAuth(String login, String password) {
        if(isAuthorize(login, password)) {
            String id = dao.getId(login);
            createUserDirIfNotExist(id);
            loginRootDir.put(login, Path.of(id));
            loginDir.put(login, Path.of(id));
            return true;
        }
        return false;
    }

    public void authorize(User user) {
        dao.insertUser(user.getLogin(), user.getPassword(), user.getFirstName(), user.getSecondName(), user.getEmail());
    }
    
    private void createUserDirIfNotExist(String id) {
        try {
            if(!new File(id).exists())
                Files.createDirectory(Path.of(id));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isAuthorize(String login, String password) {
        return dao.isExist(login, password);
    }
}
