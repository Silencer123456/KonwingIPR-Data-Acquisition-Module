package knowingipr.dam.database;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Provides a connection to SQL data sources database.
 */
public class DbManager {

    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private static final int LoginTimeout = 10;

    private Connection connection;

    /**
     * Initiates a connection to the database.
     * The connection information is read from an external
     * configuration file
     *
     * @return
     * @throws ClassNotFoundException
     */
    public Connection createConnection() throws ClassNotFoundException {
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
            LOGGER.warning("Unable to find " + configPath + " file in " + configPath);
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

        Connection connection = null;
        try {
            connection = DriverManager.getConnection(host, username, password);
            LOGGER.info("CONNECTION: " + connection);
        } catch (SQLException e) {
            LOGGER.severe("Could not establish connection to the database.");
            System.exit(1);
        }

        this.connection = connection;
        return connection;
    }

    public void closeConnection() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
