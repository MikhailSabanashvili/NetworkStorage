module com.geekbrains.cloud.june.cloudapplication {
    requires javafx.controls;
    requires javafx.fxml;
    requires io.netty.codec;
    requires com.geekbrains.cloud.june.model;

    opens com.geekbrains.cloud.june.cloudapplication to javafx.fxml;
    exports com.geekbrains.cloud.june.cloudapplication;
    exports com.geekbrains.cloud.june.cloudapplication.controller;
    opens com.geekbrains.cloud.june.cloudapplication.controller to javafx.fxml;
    exports com.geekbrains.cloud.june.cloudapplication.network;
    opens com.geekbrains.cloud.june.cloudapplication.network to javafx.fxml;
}