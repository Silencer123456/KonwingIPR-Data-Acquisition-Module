package knowingipr.dam.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ErrorController {

    @FXML
    private Label errorMessage ;

    /**
     * Sets the text of the error message label
     *
     * @param text
     */
    public void setErrorText(String text) {
        errorMessage.setText(text);
    }

    @FXML
    private void close() {
        errorMessage.getScene().getWindow().hide();
    }
}
