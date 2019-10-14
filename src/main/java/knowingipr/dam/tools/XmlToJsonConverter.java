package knowingipr.dam.tools;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Logger;

public class XmlToJsonConverter {

    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);


    /**
     * Converts all xml files in the specified directory to JSON format
     * amd moves it to the destination path. Includes files from subdirectories.
     *
     * @param pathToDir       - Path to the parent directory from which to locate xml files
     * @param destinationPath - The destination path, where converted JSON files should be moved
     * @throws IOException
     */
    public void convertDirectory(String pathToDir, String destinationPath) throws IOException {
        // TODO: Extract listing all files to generic method
        File dir = new File(pathToDir);
        if (dir.isFile()) {
            System.err.println("The path " + pathToDir + " is not a directory.");
            return;
        }

        String[] extensions = new String[]{"xml"};
        List<File> files = (List<File>) FileUtils.listFiles(dir, extensions, true);

        for (File file : files) {
            String path = file.getParent();
            path = path.substring(path.lastIndexOf(file.getParentFile().getName()));
            path = destinationPath + "\\" + path + "\\";

            LOGGER.info("Converting: " + path);
            convert(file.getCanonicalPath(), path, false);
        }
    }

    /**
     * Converts a XML file to JSON.  It supports dtd entities.
     *
     * @param pathToXmlFile   - The path to the XML file
     * @param destinationPath - The destination path, where the converted JSON file should be saved.
     *                        The path has to end with the trailing slash '/'.
     * @param overwrite       - Flag specifying, whether the existing file in the destination path should be overwritten
     *                        if it exists.
     */
    private void convert(String pathToXmlFile, String destinationPath, boolean overwrite) {
        File xmlFile = new File(pathToXmlFile);
        if (!xmlFile.isFile()) {
            LOGGER.warning("The path " + pathToXmlFile + " is not a file.");
        }
        String nameWithoutExt = FilenameUtils.getBaseName(xmlFile.getName());

        File destFile = new File(destinationPath);
        destFile.mkdirs();

        String jsonFilePath = destinationPath + nameWithoutExt + ".json";

        // If already exists, skip it if overwrite is false
        if (!overwrite && new File(jsonFilePath).exists()) {
            LOGGER.info("File " + jsonFilePath + " already exists. Skipping...");
            return;
        }

        try {
            String xml = FileUtils.readFileToString(new File(pathToXmlFile), "utf-8");

            JSONObject xmlJSONObj = XML.toJSONObject(xml);
            xml = xmlJSONObj.toString(4);

            save(xml, jsonFilePath);

            xmlJSONObj = null;
            xml = null;
            System.gc(); // Need to perform garbage collection

        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.warning("There was an error reading the XML file " + pathToXmlFile + ".");
        } catch (JSONException e) {
            e.printStackTrace();
            LOGGER.warning("There was an error converting the XML file " + pathToXmlFile + " to JSON.");
        }
    }

    /**
     * Saves a file to the file system in path
     *
     * @param json     - contents of the file to be saved
     * @param filepath - path, where the file should be saved
     * @throws FileNotFoundException
     */
    private void save(String json, String filepath) throws FileNotFoundException {
        try (PrintWriter out = new PrintWriter(filepath)) {
            out.println(json);
        }
    }
}
