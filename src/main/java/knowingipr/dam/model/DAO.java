package knowingipr.dam.model;

/**
 * Abstract data access object providing access to the database
 */
public interface DAO {
    void setup() throws Exception;

    /**
     * Connects to the database
     *
     * @throws Exception if error connecting
     */
    void connect() throws Exception;

    /**
     * Closes the connection to the database
     *
     * @throws Exception - If error when closing
     */
    void close() throws Exception;
}
