package com.geekbrains.cloud.june.cloudapplication;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ChatApplication extends Application {
    public static boolean isEntered;

    @Override
    public void start(Stage stage) throws IOException {
        while (!isEntered) {
            login(stage);
        }
        stage.close();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("file_list.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 750, 750);
        stage.setTitle("Network Storage");
        stage.setScene(scene);
        stage.show();
    }

    public void login(Stage stage) throws IOException {
        isEntered = false;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 750, 750);
        stage.setTitle("Login");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}