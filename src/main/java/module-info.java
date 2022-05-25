module com.company.networkstorage {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.company.networkstorage to javafx.fxml;
    exports com.company.networkstorage;
}