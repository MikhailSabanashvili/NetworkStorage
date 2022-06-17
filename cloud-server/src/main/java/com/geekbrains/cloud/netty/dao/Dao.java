package com.geekbrains.cloud.netty.dao;

public interface Dao {
    void insertUser(String login, String password, String firstName, String secondName, String email);
    boolean isExist(String login, String password);
    String getId(String login);
}
