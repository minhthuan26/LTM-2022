module com.example.ltm2022client {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires json.simple;
    requires org.json;

    opens com.ltm2022client to javafx.fxml;
    exports com.ltm2022client;

    opens com.ltm2022client.application to javafx.fxml;
    exports com.ltm2022client.application;
}