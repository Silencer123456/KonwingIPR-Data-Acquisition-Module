package knowingipr.dam.controller;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import knowingipr.dam.model.DataSourceModel;
import knowingipr.dam.tools.XmlToJsonConverter;
import knowingipr.dam.tools.ZipHandler;

import java.io.IOException;

/**
 * Controller for the tools bar
 */
public class ToolsController {

    private DataSourceModel model;

    @FXML
    public TextField targetDirectoryTextField;
    @FXML
    public TextField sourceDirectoryTextField;
    @FXML
    public Button zipExtractButton;
    @FXML
    public Button xmlToJsonButton;

    public void initModel(DataSourceModel model) {
        this.model = model;
    }


    public void onXmlToJsonButtonClick(ActionEvent actionEvent) {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                XmlToJsonConverter.convertDirectory(sourceDirectoryTextField.getText(), targetDirectoryTextField.getText());
                return null;
            }
        };
        new Thread(task).start();

        task.setOnSucceeded(evt -> {
            System.out.println(task.getValue());
            model.currentStatusProperty().setValue("Conversion complete");
        });

        task.setOnFailed(evt -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error while reading files from directory: " + task.getException().getMessage());
            alert.showAndWait();
            System.err.println("The task failed with the following exception:");
            task.getException().printStackTrace(System.err);
        });
    }

    public void onZipExtractButtonClick(ActionEvent actionEvent) {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                ZipHandler.extractDirectory(sourceDirectoryTextField.getText(), targetDirectoryTextField.getText());
                return null;
            }
        };
        new Thread(task).start();

        task.setOnSucceeded(evt -> {
            System.out.println(task.getValue());
            model.currentStatusProperty().setValue("Extraction complete");
        });

        task.setOnFailed(evt -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error while reading files from directory: " + task.getException().getMessage());
            alert.showAndWait();
            System.err.println("The task failed with the following exception:");
            task.getException().printStackTrace(System.err);
        });
    }
}
