package com.geekbrains.cloud;

import lombok.Data;

@Data
public class FileDeleteRequest implements CloudMessage {
    private final String name;
    private final String login;
}
