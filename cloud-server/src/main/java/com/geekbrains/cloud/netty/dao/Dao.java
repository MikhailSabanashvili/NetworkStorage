package com.geekbrains.cloud.netty.dao;

public interface Dao {
    void insertUser(String login, String password, String firstName, String secondName, String email);
    User getUser(String login);
}
