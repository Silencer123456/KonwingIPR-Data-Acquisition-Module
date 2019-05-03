package knowingipr.dam;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import knowingipr.dam.controller.*;
import knowingipr.dam.logging.MyLogger;
import knowingipr.dam.model.DataSourceDAO;
import knowingipr.dam.model.DataSourceModel;
import knowingipr.dam.model.IDataSourceDAO;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;

public class Main extends Application {

    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    @Override
    public void start(Stage primaryStage) throws Exception{
        Thread.setDefaultUncaughtExceptionHandler(Main::showError);
        MyLogger.setup("dam");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        BorderPane centerPane = new BorderPane();
        root.setCenter(centerPane);

        FXMLLoader listLoader = new FXMLLoader(getClass().getResource("/list.fxml"));
        centerPane.setCenter(listLoader.load());
        ListController listController = listLoader.getController();

        FXMLLoader toolLoader = new FXMLLoader(getClass().getResource("/tools.fxml"));
        centerPane.setBottom(toolLoader.load());
        ToolsController toolsController = toolLoader.getController();

        FXMLLoader detailLoader = new FXMLLoader(getClass().getResource("/detail.fxml"));
        root.setRight(detailLoader.load());
        DetailController detailController = detailLoader.getController();

        FXMLLoader statusBarLoader = new FXMLLoader(getClass().getResource("/statusBar.fxml"));
        root.setBottom(statusBarLoader.load());
        StatusBarController statusBarController = statusBarLoader.getController();

        IDataSourceDAO sourceDAO = new DataSourceDAO();

        DataSourceModel model = new DataSourceModel(sourceDAO);
        detailController.initModel(model);
        listController.initModel(model);
        statusBarController.initModel(model);
        toolsController.initModel(model);

        Scene scene = new Scene(root, 900, 700);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Data Administrator");
        primaryStage.show();

        /*Parent root = FXMLLoader.load(getClass().getResource("/sample.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();*/
    }

    private static void showError(Thread t, Throwable e) {
        System.err.println("***Default exception handler***");
        if (Platform.isFxApplicationThread()) {
            showErrorDialog(e);
        } else {
            System.err.println("An unexpected error occurred in "+t);

        }
    }

    @Override
    public void stop() {
        LOGGER.info("Closing the application");
        Platform.exit();
    }

    private static void showErrorDialog(Throwable e) {
        StringWriter errorMsg = new StringWriter();
        e.printStackTrace(new PrintWriter(errorMsg));
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/error.fxml"));
        try {
            Parent root = loader.load();
            ((ErrorController)loader.getController()).setErrorText(errorMsg.toString());
            dialog.setScene(new Scene(root, 250, 400));
            dialog.show();
        } catch (IOException exc) {
            exc.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
