package knowingipr.dam;

import knowingipr.data.connection.MongoDbConnection;
import knowingipr.data.connection.SourceDbConnection;
import knowingipr.data.exception.MappingException;
import knowingipr.data.loader.SourceDbLoader;
import knowingipr.data.loader.UsptoLoader;

import java.io.IOException;

public class ConsoleMain {

    private SourceDbConnection dbConnection = new MongoDbConnection();

    public ConsoleMain(String[] args) {
        SourceDbLoader sourceDbLoader = new UsptoLoader(dbConnection, args[2], args[0]);
        try {
            sourceDbLoader.loadFromDirectory(args[1], new String[]{"json"});
        } catch (IOException | MappingException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Invalid number of arguments");
            showHelp();
            return;
        }

        new ConsoleMain(args);
    }

    private static void showHelp() {
        System.out.println("To run, use parameters: [collection name] [path to directory to load] [path to the mapping file]");
        System.out.println("For example: patent E:/Patent E:/meta/patent/mapping.json");
    }


}
