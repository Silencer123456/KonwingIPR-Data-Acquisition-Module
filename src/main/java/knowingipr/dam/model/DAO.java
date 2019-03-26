package knowingipr.dam.model;

public interface DAO {
    void setup() throws Exception;

    /**
     * Connects to the database
     * @throws Exception if error connecting
     */
    void connect() throws Exception;

    /**
     * Closes the connection
     * @throws Exception - If error when closing
     */
    void close() throws Exception;
}
