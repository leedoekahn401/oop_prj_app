module project.app.humanelogistics {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.graphics;
    requires org.jfree.jfreechart;
    requires org.mongodb.driver.sync.client;
    requires org.mongodb.bson;
    requires org.mongodb.driver.core;

    opens project.app.humanelogistics to javafx.fxml;
    exports project.app.humanelogistics;
    exports project.app.humanelogistics.controller;
    opens project.app.humanelogistics.controller to javafx.fxml;
    exports project.app.humanelogistics.model;
    opens project.app.humanelogistics.model to javafx.fxml;
}