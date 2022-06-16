package com.geekbrains.cloud.netty.service;

import com.geekbrains.cloud.netty.dao.DaoImpl;

public class UserService {
    private DaoImpl dao;

    public UserService() {
        this.dao = new DaoImpl();
    }

    public boolean isAuth(String login, String password) {
        return isAuthorize(login, password);
    }

    public void authorize(String login, String password, String firstName, String secondName, String email) {
        dao.insertUser(login, password, firstName, secondName, email);
    }

    private boolean isAuthorize(String login, String password) {
        return dao.isExist(login, password);
    }
}
