package knowingipr.dam.logging;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.logging.*;

/**
 * Provides logging to the console and file
 */
public class MyLogger {
    static private FileHandler fileTxt;
    static private SimpleFormatter formatterTxt;

    public static void setup(String fileSuffix) throws IOException {
        formatterTxt = new SimpleFormatter() {
            private static final String format = "[%1$tF %1$tT] [%2$-7s] %3$s %n";

            @Override
            public synchronized String format(LogRecord lr) {
                return String.format(format,
                        new Date(lr.getMillis()),
                        lr.getLevel().getLocalizedName(),
                        lr.getMessage()
                );
            }
        };

        // get the global logger to configure it
        Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

        // suppress the logging output to the console
        Logger rootLogger = Logger.getLogger("");
        Handler[] handlers = rootLogger.getHandlers();
        if (handlers[0] instanceof ConsoleHandler) {
            rootLogger.removeHandler(handlers[0]);
        }

        logger.setLevel(Level.ALL);

        setupConsoleHandler(logger);
        setupFileHandler(logger, fileSuffix);
    }

    private static void setupConsoleHandler(Logger logger) {
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.ALL);
        consoleHandler.setFormatter(formatterTxt);
        logger.addHandler(consoleHandler);
    }

    private static void setupFileHandler(Logger logger, String fileSuffix) {
        try {
            String logsDir = "logs";
            File directory = new File(logsDir);
            if (!directory.exists()) {
                directory.mkdir();
            }

            Long timeStamp = System.currentTimeMillis();

            FileHandler fhandler = new FileHandler(
                    logsDir + "//log-" + timeStamp + "-" + fileSuffix + ".log");
            fhandler.setFormatter(formatterTxt);
            logger.addHandler(fhandler);

        } catch (IOException | SecurityException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
}
