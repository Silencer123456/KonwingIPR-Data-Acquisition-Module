package knowingipr.data.loader;


/**
 * Specifies the connection to the main source database,
 * where the source data are stored.
 */
public interface SourceDbConnection {

    /**
     * Connects to the source database
     */
    void connect();

    /**
     * Inserts the provided data to the target source database.
     * @param loadArgs - The data structure to be loaded into the target database
     */
    void insert(IDbLoadArgs loadArgs);

    /**
     * Disconnects from the source database
     */
    void disconnect();
}
