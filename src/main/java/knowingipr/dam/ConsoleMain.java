package knowingipr.dam;

import knowingipr.data.loader.MongoDbConnection;
import knowingipr.data.loader.PatentLoader;
import knowingipr.data.loader.SourceDbConnection;
import knowingipr.data.loader.SourceDbLoader;

import java.io.IOException;

public class ConsoleMain {

    SourceDbConnection dbConnection = new MongoDbConnection();

    public ConsoleMain(String[] args) {
        SourceDbLoader sourceDbLoader = new PatentLoader(dbConnection, args[2], args[0]);
        try {
            sourceDbLoader.loadFromDirectory(args[1], new String[]{"json"});
        } catch (IOException e) {
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
