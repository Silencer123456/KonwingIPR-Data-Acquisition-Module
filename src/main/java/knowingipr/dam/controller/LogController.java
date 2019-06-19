package knowingipr.dam.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

import java.util.ArrayList;
import java.util.List;

public class LogController {

    @FXML
    public TextArea logTextArea;

    List<String> logMessages;

    public LogController() {
        logMessages = new ArrayList<>();
    }

    /**
     * Appends a new log message to the text area
     *
     * @param logMessage - text to append
     */
    public void appendLogMessage(String logMessage) {
        logTextArea.appendText(logMessage);
    }
}
