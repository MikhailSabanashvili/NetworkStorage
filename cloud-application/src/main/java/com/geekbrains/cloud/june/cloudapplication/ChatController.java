package com.geekbrains.cloud.june.cloudapplication;

import com.geekbrains.cloud.CloudMessage;
import com.geekbrains.cloud.FileMessage;
import com.geekbrains.cloud.FileRequest;
import com.geekbrains.cloud.ListFiles;
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
import java.util.*;

public class ChatController implements Initializable {


    @FXML
    ListView<String> clientView;

    @FXML
    ListView<String> serverView;

    private Network network;
    private String homeDir;
    private String root_dir;

    private void dragAndDropClientServer() {
        dragAndDrop(clientView, serverView);
    }

    private void dragAndDropServerClient() {
        dragAndDrop(serverView, clientView);
    }

    private void dragAndDrop(ListView<String> listView1, ListView<String> listView2) {
        listView1.setOnDragDetected(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                Dragboard db = listView1.startDragAndDrop(TransferMode.ANY);

                ClipboardContent content = new ClipboardContent();
                content.putString(listView1.getSelectionModel().getSelectedItem());
                db.setContent(content);

                event.consume();
            }
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
            network.write(new FileMessage(Path.of(homeDir).resolve(file)));
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
                                    network.write(new FileRequest(true,".."));
                                    return;
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            char[] arr = homeDir.toCharArray();
                            for (int i = arr.length - 1; i >= 0; i--) {
                                if(arr[i] == '\\') {
                                    arr[i] = '!';
                                    break;
                                }

                                arr[i] = '_';
                            }

                            homeDir = String.valueOf(arr).split("!")[0];
                            listView.getItems().addAll(getFiles(homeDir, root_dir.equals(homeDir)));
                            return;
                        }
                        if(isServer) {
                            try {
                                FileRequest fileRequest = new FileRequest(true, fileName);
                                network.write(fileRequest);
                                return;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        File dir = new File(homeDir);
                        File[] arrFiles = dir.listFiles();
                        assert arrFiles != null;
                        Optional<File> lst = Arrays.stream(arrFiles)
                                .filter(x -> x.getName().equals(fileName)).findFirst();
                        if(lst.isPresent() && lst.get().isDirectory()) {
                            listView.getItems().clear();
                            homeDir = String.valueOf(Path.of(homeDir).resolve(lst.get().getName()));
                            listView.getItems().addAll(getFiles(homeDir, homeDir.equals(root_dir)));

                        }
                    }
                }
            }
        });
    }

    private void readLoop() {
        try {
            while (true) {
                dragAndDropClientServer();
                dragAndDropServerClient();
                isClickedOnClient();
                isClickedOnServer();
                CloudMessage message = network.read();
                if (message instanceof ListFiles listFiles) {
                    Platform.runLater(() -> {
                        serverView.getItems().clear();
                        serverView.getItems().addAll(listFiles.getFiles());
                    });
                } else if (message instanceof FileMessage fileMessage) {
                    Path current = Path.of(homeDir).resolve(fileMessage.getName());
                    Files.write(current, fileMessage.getData());
                    Platform.runLater(() -> {
                        clientView.getItems().clear();
                        clientView.getItems().addAll(getFiles(homeDir, homeDir.equals(root_dir)));
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
            homeDir = "client_files";
            root_dir = homeDir;
            clientView.getItems().clear();
            clientView.getItems().addAll(getFiles(homeDir, homeDir.equals(root_dir)));
            network = new Network(8189);
            Thread readThread = new Thread(this::readLoop);
            readThread.setDaemon(true);
            readThread.start();

        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private List<String> getFiles(String dir, boolean isRootDir) {
        String[] list;
        if(!isRootDir) {
            String[] fileList = new File(dir).list();
            assert fileList != null;
            list = new String[fileList.length + 1];
            list[0] = "..";
            for (int i = 1, j = 0; i < list.length; i++, j++) {
                list[i] = fileList[j];
            }
            return Arrays.asList(list);
        }
        list = new File(dir).list();
        assert list != null;
        return Arrays.asList(list);
    }
}