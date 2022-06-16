package com.geekbrains.cloud.june.cloudapplication;

import com.geekbrains.cloud.*;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {
    @FXML
    TextField loginL;
    @FXML
    TextField passwordL;
    @FXML
    TextField loginA;
    @FXML
    TextField passwordA;
    @FXML
    TextField firstName;
    @FXML
    TextField secondName;
    @FXML
    TextField email;
    @FXML
    TextArea error;
    @FXML
    Button login;
    @FXML
    Button authorize;

    private Network network;

    public void authenticate() {
        try {
            network.write(new AuthRequest(loginL.getText(), passwordL.getText()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void authorize() {
        try {
            network.write(new AuthorizeRequest(loginA.getText(), passwordA.getText(), firstName.getText(),
                    secondName.getText(), email.getText()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readLoop() {
        try {
            while (true) {
                CloudMessage message = network.read();
                if(message instanceof AuthResponse response) {
                    if(!response.isAuth())
                        error.setText("You are not registered. Please, log in");
                    else {
                        break;
                    }
                } else if(message instanceof AuthorizeResponse response) {
                    if(!response.isSuccess())
                        error.setText("Something went wrong. Please, log in again");
                    else {
                        break;
                    }
                }

            }
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("file_list.fxml"));
            Parent root = fxmlLoader.load();
            ChatApplication.scene.setRoot(root);
            ChatApplication.stage.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            login.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    authenticate();
                }
            });
            authorize.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    authorize();
                }
            });
            network = new Network(8189);
            Thread readThread = new Thread(this::readLoop);
            readThread.setDaemon(true);
            readThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
