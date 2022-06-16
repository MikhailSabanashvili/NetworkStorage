package com.geekbrains.cloud;

import lombok.Data;

@Data
public class AuthorizeRequest implements CloudMessage {
    private final String login;
    private final String password;
    private final String firstName;
    private final String secondName;
    private final String email;
}
