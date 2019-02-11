package knowingipr.dam.tools;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipHandler {

    /**
     * Selects all the ZIP files from the directory and its subdirectories and
     * extracts their contents to the destination path.
     * Maintains the subdirectory structure in the destination.
     *
     * @param pathToDir     - Source path to the parent directory from which to select the files
     * @param extractedPath - The destination path, where the extracted files should be moved
     * @throws IOException if the source directory does not exist
     */
    public static void extractDirectory(String pathToDir, String extractedPath) throws IOException {
        File dir = new File(pathToDir);
        if (dir.isFile()) {
            System.err.println("The path " + pathToDir + " is not a directory.");
            return;
        }

        String[] extensions = new String[]{"zip"};
        String dirName = dir.getName();
        List<File> files = (List<File>) FileUtils.listFiles(dir, extensions, true);

        for (File file : files) {
            String s = file.getParent();
            s = s.substring(s.lastIndexOf(dirName));
            s = extractedPath + s + "/" + FilenameUtils.getBaseName(file.getName()) + "/";

            System.out.println(s);
            extractFile(file.getCanonicalPath(), s);
        }
    }

    /**
     * Extracts single file from the specified path.
     *
     * @param zipPath - Path to the ZIP file to be extracted
     * @param outPath - The destination path, where the contents of the file should be moved
     * @throws IOException if the path to the ZIP file does not exist
     */
    private static void extractFile(String zipPath, String outPath) throws IOException {
        ZipFile zipFile = new ZipFile(zipPath);
        Enumeration<?> enu = zipFile.entries();
        while (enu.hasMoreElements()) {
            ZipEntry zipEntry = (ZipEntry) enu.nextElement();

            String name = zipEntry.getName();
            long size = zipEntry.getSize();
            long compressedSize = zipEntry.getCompressedSize();
            System.out.printf("name: %-20s | size: %6d | compressed size: %6d\n",
                    name, size, compressedSize);

            File file = new File(outPath + name);
            if (name.endsWith("/")) {
                file.mkdirs();
                continue;
            }

            File parent = file.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }

            InputStream is = zipFile.getInputStream(zipEntry);
            FileOutputStream fos = new FileOutputStream(file);
            byte[] bytes = new byte[1024];
            int length;
            while ((length = is.read(bytes)) >= 0) {
                fos.write(bytes, 0, length);
            }
            is.close();
            fos.close();

        }
        zipFile.close();
    }
}
