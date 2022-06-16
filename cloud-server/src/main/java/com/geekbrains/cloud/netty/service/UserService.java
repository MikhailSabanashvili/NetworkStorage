package com.geekbrains.cloud.netty.service;

import com.geekbrains.cloud.netty.dao.DaoImpl;
import com.geekbrains.cloud.netty.dao.User;

public class UserService {
    private DaoImpl dao;

    public UserService() {
        this.dao = new DaoImpl();
    }

    public boolean isAuth(String login, String password) {
        return isAuthorize(login, password);
    }

    public void authorize(User user) {
        dao.insertUser(user.getLogin(), user.getPassword(), user.getFirstName(), user.getSecondName(), user.getEmail());
    }

    private boolean isAuthorize(String login, String password) {
        return dao.isExist(login, password);
    }
}
