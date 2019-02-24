package knowingipr.dam.controller.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import knowingipr.dam.model.DataSource;
import knowingipr.dam.model.DataSourceModel;

import java.io.IOException;

public class ListViewData {

    @FXML
    public Label sourceNameLabel;
    @FXML
    public Label collectionLabel;
    @FXML
    public Label expiredLabel;
    @FXML
    public HBox hBox;

    private Tooltip expiredTooltip;

    private DataSourceModel model;

    public ListViewData(DataSourceModel model) {
        this.model = model;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/listview/listCellItem.fxml"));

        fxmlLoader.setController(this);
        try
        {
            fxmlLoader.load();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        expiredTooltip = new Tooltip();
        expiredTooltip.setText("This data source is expired.");
    }

    public void setData(DataSource source) {
        sourceNameLabel.setText(source.getName());
        collectionLabel.setText(source.getCategoryType());

        if (model.isSourceExpired(source)) {
            Image image = new Image("icons/warning.png");
            ImageView view = new ImageView(image);
            view.setFitWidth(20);
            view.setFitHeight(20);
            expiredLabel.setGraphic(view);
            expiredLabel.setTooltip(expiredTooltip);
        }
        else {
            expiredLabel.setGraphic(null);
            expiredLabel.setTooltip(null);
        }

        //expiredLabel.setText(model.isSourceExpired(source)+"");
    }

    public HBox getBox() {
        return hBox;
    }
}
