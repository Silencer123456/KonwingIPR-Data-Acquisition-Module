package knowingipr.dam.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import knowingipr.dam.tools.XmlToJsonConverter;
import knowingipr.dam.tools.ZipHandler;

import java.io.IOException;

/**
 * Controller for the tools bar
 */
public class ToolsController {

    @FXML
    public TextField targetDirectoryTextField;
    @FXML
    public TextField sourceDirectoryTextField;
    @FXML
    public Button zipExtractButton;
    @FXML
    public Button xmlToJsonButton;

    public void onXmlToJsonButtonClick(ActionEvent actionEvent) {
        try {
            XmlToJsonConverter.convertDirectory(sourceDirectoryTextField.getText(), targetDirectoryTextField.getText());
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error while reading files from directory: " + e.getMessage());
            alert.showAndWait();
            e.printStackTrace();
        }
    }

    public void onZipExtractButtonClick(ActionEvent actionEvent) {
        try {
            ZipHandler.extractDirectory(sourceDirectoryTextField.getText(), targetDirectoryTextField.getText());
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error while reading files from directory: " + e.getMessage());
            alert.showAndWait();
            e.printStackTrace();
        }
    }
}
