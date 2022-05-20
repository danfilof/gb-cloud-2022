package ru.gb.cloud;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent parent = FXMLLoader.load(getClass().getResource("main.fxml"));
        primaryStage.setScene(new Scene(parent));
        primaryStage.setTitle("GB Cloud Service");
        parent.getStylesheets().add(getClass().getResource("cssStyle.css").toExternalForm());
        primaryStage.show();
    }
}
