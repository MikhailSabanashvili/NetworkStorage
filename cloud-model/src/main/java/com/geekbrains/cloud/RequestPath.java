package com.geekbrains.cloud;

import lombok.Data;

@Data
public class RequestPath implements CloudMessage {
    private final String login;
}
