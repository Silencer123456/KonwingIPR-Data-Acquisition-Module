package knowingipr.dam;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import knowingipr.dam.controller.DetailController;
import knowingipr.dam.controller.ListController;
import knowingipr.dam.model.DataModel;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        BorderPane root = new BorderPane();

        FXMLLoader listLoader = new FXMLLoader(getClass().getResource("/list.fxml"));
        root.setCenter(listLoader.load());
        ListController listController = listLoader.getController();

        FXMLLoader detailLoader = new FXMLLoader(getClass().getResource("/detail.fxml"));
        root.setRight(detailLoader.load());
        DetailController detailController = detailLoader.getController();

        DataModel model = new DataModel();
        listController.initModel(model);
        detailController.initModel(model);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        /*Parent root = FXMLLoader.load(getClass().getResource("/sample.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();*/
    }


    public static void main(String[] args) {
        launch(args);
    }
}
