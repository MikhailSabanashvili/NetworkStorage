package com.geekbrains.cloud;

import lombok.Data;

@Data
public class LoginResponse implements CloudMessage {
    private final String login;
}
