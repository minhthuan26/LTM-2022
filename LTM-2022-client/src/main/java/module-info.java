module com.example.ltm2022client {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires org.json;
    requires java.desktop;
    requires opencv;
    requires org.jsoup;
    requires org.apache.commons.io;

    opens com.ltm2022client to javafx.fxml;
    exports com.ltm2022client;

    opens com.ltm2022client.application to javafx.fxml;
    exports com.ltm2022client.application;
}