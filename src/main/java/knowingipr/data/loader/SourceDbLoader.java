package knowingipr.data.loader;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Abstract class for loading data to the database.
 */
public abstract class SourceDbLoader {
    protected final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    protected SourceDbConnection dbConnection;

    public SourceDbLoader(SourceDbConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    /**
     * Inserts to the target database.
     * @param file - File to be loaded into the database
     * @throws IOException if the file loading fails
     */
    public abstract void insertFromFile(File file) throws IOException;

    /**
     * Loads all the files with the specified extension from the directory (including
     * its subdirectories).
     * @param dirPath - The directory path from which to get the files
     * @param extensions - The extensions of the files to get
     * @throws IOException if there was error reading or accessing files
     */
    public abstract void loadFromDirectory(String dirPath, String[] extensions) throws IOException;
}
