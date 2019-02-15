package knowingipr.dam.controller.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
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
    }

    public void setData(DataSource source) {
        sourceNameLabel.setText(source.getName());
        collectionLabel.setText(source.getCategoryType());
        expiredLabel.setText(model.isSourceExpired(source)+"");
    }

    public HBox getBox() {
        return hBox;
    }
}
