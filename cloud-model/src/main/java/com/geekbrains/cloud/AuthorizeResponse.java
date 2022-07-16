package com.geekbrains.cloud;

import lombok.Data;

@Data
public class AuthorizeResponse implements CloudMessage {
    private final boolean isSuccess;
}
