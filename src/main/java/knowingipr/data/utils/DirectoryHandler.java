package knowingipr.data.utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * Helper class for manipulation with file system.
 */
public class DirectoryHandler {

    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    /**
     * Lists all the files with specified extension from the directory.
     * @param dirPath - The path to the directory from which to list files
     * @param extensions - The extensions of the listed files
     * @param subDirs - Indicates if the listing should include subdirectories
     * @return List of files from the directory with specified extension. If the directory path
     * does not point to directory, an empty list is returned.
     */
    public static List<File> ListFilesFromDirectory(String dirPath, String[] extensions, boolean subDirs) {
        File dir = new File(dirPath);
        if (dir.isFile() || !dir.exists()) {
            LOGGER.warning("The path " + dirPath + " is not a directory or does not exist.");
            return Collections.emptyList();
        }

        //String[] extensions = new String[] { "json" };
        return (List<File>) FileUtils.listFiles(dir, extensions, true);
    }
}
