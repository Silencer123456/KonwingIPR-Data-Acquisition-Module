package knowingipr.dam.logging;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/**
 * Creates a new logging handler to the specified JavaFX Text Area
 *
 * @author Stepan Baratta
 * created on 6/19/2019
 */
public class TextAreaHandler extends Handler {
    Formatter formatter = new SimpleFormatter();

    private TextArea textArea;

    private int linesCounter = 0;

    public TextAreaHandler(TextArea area) {
        this.textArea = area;
    }

    @Override
    public void publish(LogRecord record) {
        if (textArea == null) return;

        Platform.runLater(() -> {
            if (linesCounter++ > 1000) {
                textArea.clear();
            }
            textArea.appendText(formatter.format(record));
        });
    }

    @Override
    public void flush() {

    }

    @Override
    public void close() throws SecurityException {

    }
}
