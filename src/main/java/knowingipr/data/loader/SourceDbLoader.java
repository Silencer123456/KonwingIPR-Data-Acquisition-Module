package knowingipr.data.loader;

import com.fasterxml.jackson.databind.JsonNode;
import knowingipr.data.exception.MappingException;
import knowingipr.data.utils.DirectoryHandler;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Abstract class for loading data to the database.
 */
public abstract class SourceDbLoader {
    protected final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    protected SourceDbConnection dbConnection;
    protected String mappingFilePath;

    public SourceDbLoader(SourceDbConnection dbConnection, String mappingFile) {
        this.dbConnection = dbConnection;
        this.mappingFilePath = mappingFile;
    }

    /**
     * Inserts list of documents to the target database.
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
    public void loadFromDirectory(String dirPath, String[] extensions) throws IOException {
        List<File> files = DirectoryHandler.ListFilesFromDirectory(dirPath, extensions, true);
        for (File file : files) {
            LOGGER.info("Processing " + file.getCanonicalPath());
            insertFromFile(file);
        }
    }

    public abstract void preprocessNode(JsonNode nodeToPreprocess) throws MappingException;
}
