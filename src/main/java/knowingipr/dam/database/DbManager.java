package knowingipr.dam.database;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

public class DbManager {

    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private static final int LoginTimeout = 10;

    public Connection createConnection() throws SQLException, ClassNotFoundException {
        String configPath = "mydb.cfg";
        Properties prop = new Properties();
        String host;
        String username;
        String password;
        String driver;
        try {
            prop.load(new FileInputStream(configPath));
            host = prop.getProperty("host");
            username = prop.getProperty("username");
            password = prop.getProperty("password");
            driver = prop.getProperty("driver");
        } catch (IOException e) {
            LOGGER.warning("Unable to find mydb.cfg file in " + configPath);
            e.printStackTrace();

            host = "Unknown HOST";
            username = "Unknown USER";
            password = "Unknown PASSWORD";
            driver = "Unknown DRIVER";
        }

        LOGGER.info("host: " + host + "\nusername: " + username + "\npassword: " + password + "\ndriver: " + driver);

        Class.forName(driver);
        LOGGER.info("--------------------------");
        LOGGER.info("DRIVER: " + driver);
        LOGGER.info("Set Login Timeout: " + LoginTimeout);
        DriverManager.setLoginTimeout(LoginTimeout);
        Connection connection = DriverManager.getConnection(host, username, password);
        LOGGER.info("CONNECTION: " + connection);

        return connection;
    }
}
