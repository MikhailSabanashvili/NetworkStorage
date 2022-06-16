package com.geekbrains.cloud.june.cloudapplication;

import com.geekbrains.cloud.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.input.*;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class ChatController implements Initializable {


    @FXML
    ListView<String> clientView;

    @FXML
    ListView<String> serverView;

    private Network network;
    private Path currentDir;
    private Path rootDir;
    private ContextMenu contextMenuClient;
    private ContextMenu contextMenuServer;
    private MenuItem itemDeleteClient;
    private MenuItem itemDeleteServer;

    private void dragAndDropClientServer() {
        dragAndDrop(clientView, serverView);
    }

    private void dragAndDropServerClient() {
        dragAndDrop(serverView, clientView);
    }

    private void deleteFileServer() {
        serverView.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
            @Override
            public void handle(ContextMenuEvent contextMenuEvent) {
                itemDeleteServer.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        String[] list = contextMenuEvent.getPickResult().getIntersectedNode().toString().split("\"");
                        String fileName = list[1];
                        try {
                            network.write(new FileDeleteRequest(fileName));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
//                        serverView.getItems().clear();
//                        serverView.getItems().addAll(getFiles(currentDir, currentDir.equals(rootDir)));
                    }
                });
            }
        });
    }

    private void deleteFileClient() {
        clientView.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
            @Override
            public void handle(ContextMenuEvent event) {
                itemDeleteClient.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        String[] list = event.getPickResult().getIntersectedNode().toString().split("\"");
                        String fileName = list[1];
                        Path file = currentDir.resolve(Path.of(fileName));
                        try {
                            Files.delete(file);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        clientView.getItems().clear();
                        clientView.getItems().addAll(getFiles(currentDir, currentDir.equals(rootDir)));
                    }
                });

            }
        });


    }

    private void showContextMenu(ListView<String> listView, ContextMenu contextMenu) {
        listView.setOnContextMenuRequested(e ->
                contextMenu.show(listView, e.getScreenX(), e.getScreenY()));
    }

    private void dragAndDrop(ListView<String> listView1, ListView<String> listView2) {
        listView1.setOnDragDetected(event -> {
            Dragboard db = listView1.startDragAndDrop(TransferMode.ANY);

            ClipboardContent content = new ClipboardContent();
            content.putString(listView1.getSelectionModel().getSelectedItem());
            db.setContent(content);

            event.consume();
        });

        listView2.setOnDragOver(new EventHandler<DragEvent>() {
            public void handle(DragEvent event) {
                if (event.getGestureSource() != listView2) {
                    event.acceptTransferModes(TransferMode.ANY);
                }

                event.consume();
            }
        });

        listView2.setOnDragDropped(new EventHandler() {
            @Override
            public void handle(Event event) {
                DragEvent dragEvent = (DragEvent) event;
                Dragboard db = dragEvent.getDragboard();
                boolean success = false;
                if (db.hasString()) {
                    if (listView1.equals(serverView)) {
                        transferFrom(serverView.getSelectionModel().getSelectedItem());
                    } else {
                        transferTo(clientView.getSelectionModel().getSelectedItem());
                    }
                    success = true;
                }
                dragEvent.setDropCompleted(success);
                dragEvent.consume();
            }
        });
    }

    private void transferTo(String file) {
        try {
            network.write(new FileMessage(currentDir.resolve(file)));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private void transferFrom(String file) {
        try {
            network.write(new FileRequest(false, file));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private void isClickedOnClient() {
        isClicked(clientView, false);
    }

    private void isClickedOnServer() {
        isClicked(serverView, true);
    }

    private void isClicked(ListView<String> listView, boolean isServer) {
        listView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){
                    if(mouseEvent.getClickCount() == 2){
                        String fileName = listView.getSelectionModel().getSelectedItem();
                        if(fileName.equals("..")) {
                            listView.getItems().clear();
                            if(isServer) {
                                try {
                                    network.write(new PathUpRequest(".."));
                                    return;
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            currentDir = currentDir.getParent();

                            listView.getItems().addAll(getFiles(currentDir, rootDir.equals(currentDir)));
                            return;
                        }
                        if(isServer) {
                            try {
                                network.write(new PathInRequest(fileName));
                                return;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        listView.getItems().clear();
                        currentDir = currentDir.resolve(fileName);
                        listView.getItems().addAll(getFiles(currentDir, currentDir.equals(rootDir)));
                    }
                }
            }
        });
    }

    private void readLoop() {
        try {
            while (true) {
                CloudMessage message = network.read();
                if (message instanceof ListFiles listFiles) {
                    Platform.runLater(() -> {
                        serverView.getItems().clear();
                        serverView.getItems().addAll(listFiles.getFiles());
                    });
                } else if (message instanceof FileMessage fileMessage) {
                    Path current = currentDir.resolve(fileMessage.getName());
                    Files.write(current, fileMessage.getData());
                    Platform.runLater(() -> {
                        clientView.getItems().clear();
                        clientView.getItems().addAll(getFiles(currentDir, currentDir.equals(rootDir)));
                    });
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            contextMenuClient = new ContextMenu();
            contextMenuServer = new ContextMenu();
            itemDeleteClient = new MenuItem("Delete");
            itemDeleteServer = new MenuItem("Delete");
            contextMenuClient.getItems().addAll(itemDeleteClient);
            contextMenuServer.getItems().addAll(itemDeleteServer);
            currentDir = Path.of("client_files");
            rootDir = currentDir;
            clientView.getItems().clear();
            clientView.getItems().addAll(getFiles(currentDir, currentDir.equals(rootDir)));
            serverView.setContextMenu(contextMenuServer);
            clientView.setContextMenu(contextMenuClient);
            showContextMenu(clientView, contextMenuClient);
            showContextMenu(serverView, contextMenuServer);
            deleteFileClient();
            deleteFileServer();
            dragAndDropClientServer();
            dragAndDropServerClient();
            isClickedOnClient();
            isClickedOnServer();
            network = new Network(8189);
            Thread readThread = new Thread(this::readLoop);
            readThread.setDaemon(true);
            readThread.start();

        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private List<String> getFiles(Path dir, boolean isRootDir) {
        List<String> files;
        if(!isRootDir) {
            try {
                files = getListFiles(dir);
                files.add(0, "..");
                return files;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            return getListFiles(dir);
        } catch (IOException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("An error was occured");
    }

    private List<String> getListFiles(Path dir) throws IOException {
        return Files.list(dir)
                .map(p -> p.getFileName().toString())
                .collect(Collectors.toList());
    }
}