package knowingipr.dam.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import knowingipr.dam.logging.TextAreaHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class LogController {

    public Button clearButton;
    @FXML
    private TextArea logTextArea;

    List<String> logMessages;

    public LogController() {
        logMessages = new ArrayList<>();
    }

    /**
     * Adds a log handler to the text area
     */
    public void addLogHandler() {
        Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        logger.addHandler(new TextAreaHandler(logTextArea));
    }

    /**
     * Appends a new log message to the text area
     *
     * @param logMessage - text to append
     */
    public void appendLogMessage(String logMessage) {
        logTextArea.appendText(logMessage + "\n");
    }

    public void onClearButtonClicked(ActionEvent actionEvent) {
        logTextArea.clear();
    }
}
