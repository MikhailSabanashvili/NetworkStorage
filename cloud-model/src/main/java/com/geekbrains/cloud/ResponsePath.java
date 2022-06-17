package com.geekbrains.cloud;

import lombok.Data;

import java.nio.file.Path;

@Data
public class ResponsePath implements CloudMessage {
    private final Path path;
}
