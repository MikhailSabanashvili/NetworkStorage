package com.geekbrains.cloud;

import lombok.Data;

@Data
public class AuthResponse implements CloudMessage {
    private final boolean isAuth;
}
