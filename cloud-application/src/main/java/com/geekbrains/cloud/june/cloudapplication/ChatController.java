package com.geekbrains.cloud.june.cloudapplication;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.input.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class ChatController implements Initializable {


    @FXML
    ListView<String> clientView;

    @FXML
    ListView<String> serverView;

    private Network network;
    private String homeDir;

    private void dragAndDrop() {

        clientView.setOnDragDetected(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                Dragboard db = clientView.startDragAndDrop(TransferMode.ANY);

                ClipboardContent content = new ClipboardContent();
                content.putString(clientView.getSelectionModel().getSelectedItem());
                db.setContent(content);

                event.consume();
            }
        });

        serverView.setOnDragOver(new EventHandler<DragEvent>() {
            public void handle(DragEvent event) {
                if (event.getGestureSource() != serverView) {
                    event.acceptTransferModes(TransferMode.ANY);
                }

                event.consume();
            }
        });

        serverView.setOnDragDropped(new EventHandler() {
            @Override
            public void handle(Event event) {
                DragEvent dragEvent = (DragEvent) event;
                Dragboard db = dragEvent.getDragboard();
                boolean success = false;
                if (db.hasString()) {
                    transferFile(db.getString());
                    success = true;
                }
                dragEvent.setDropCompleted(success);
                dragEvent.consume();
            }
        });
    }

    private void transferFile(String fileName) {
        try {
            File file = new File(homeDir + "/" + fileName);
            network.getOs().writeUTF("#file#");
            network.getOs().writeUTF(fileName);
            network.getOs().writeLong(file.length());
            network.getOs().write(Files.readAllBytes(Path.of(homeDir + "/" + fileName)));
            network.getOs().flush();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private void readLoop() {
        try {
            while (true) {
                dragAndDrop();
                String command = network.readString();
                if (command.equals("#list#")) {
                    Platform.runLater(() -> serverView.getItems().clear());
                    int len = network.readInt();
                    for (int i = 0; i < len; i++) {
                        String file = network.readString();
                        Platform.runLater(() -> serverView.getItems().add(file));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Connection lost");
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            homeDir = System.getProperty("user.home");
            clientView.getItems().clear();
            clientView.getItems().addAll(getFiles(homeDir));
            network = new Network(8189);
            Thread readThread = new Thread(this::readLoop);
            readThread.setDaemon(true);
            readThread.start();

        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private List<String> getFiles(String dir) {
        String[] list = new File(dir).list();
        assert list != null;
        return Arrays.asList(list);
    }
}