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

public class XmlToJsonConverter {

    /**
     * Converts all xml files in the specified directory to JSON format
     * amd moves it to the destination path. Includes files from subdirectories.
     *
     * @param pathToDir       - Path to the parent directory from which to locate xml files
     * @param destinationPath - The destination path, where converted JSON files should be moved
     * @throws IOException
     */
    public static void convertDirectory(String pathToDir, String destinationPath) throws IOException {
        // TODO: Extract listing all files to generic method
        File dir = new File(pathToDir);
        if (dir.isFile()) {
            System.err.println("The path " + pathToDir + " is not a directory.");
            return;
        }

        String[] extensions = new String[]{"xml"};
        String dirName = dir.getName();
        List<File> files = (List<File>) FileUtils.listFiles(dir, extensions, true);

        for (File file : files) {
            String path = file.getParent();
            path = path.substring(path.lastIndexOf(dirName));
            path = destinationPath + path + "/";

            System.out.println(path);
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
    private static void convert(String pathToXmlFile, String destinationPath, boolean overwrite) {
        File xmlFile = new File(pathToXmlFile);
        if (!xmlFile.isFile()) {
            System.err.println("The path " + pathToXmlFile + " is not a file.");
        }
        String nameWithoutExt = FilenameUtils.getBaseName(xmlFile.getName());

        File destFile = new File(destinationPath);
        destFile.mkdirs();

        String jsonFilePath = destinationPath + nameWithoutExt + ".json";

        // If already exists, skip it if overwrite is false
        if (!overwrite && new File(jsonFilePath).exists()) {
            System.out.println("File " + jsonFilePath + " already exists. Skipping...");
            return;
        }

        try {
            String xml = FileUtils.readFileToString(new File(pathToXmlFile), "utf-8");

            JSONObject xmlJSONObj = XML.toJSONObject(xml);
            String jsonString = xmlJSONObj.toString(4);

            save(jsonString, jsonFilePath);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("There was an error reading the XML file " + pathToXmlFile + ".");
        } catch (JSONException e) {
            e.printStackTrace();
            System.err.println("There was an error converting the XML file " + pathToXmlFile + " to JSON.");
        }
    }

    /**
     * Saves a file to the file system in path
     *
     * @param json     - contents of the file to be saved
     * @param filepath - path, where the file should be saved
     * @throws FileNotFoundException
     */
    public static void save(String json, String filepath) throws FileNotFoundException {
        try (PrintWriter out = new PrintWriter(filepath)) {
            out.println(json);
        }
    }
}
