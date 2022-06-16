package com.geekbrains.cloud.netty.dao;

import lombok.Data;

@Data
public class User {
    private final String login;
    private final String password;
    private final String firstName;
    private final String secondName;
    private final String email;
}
