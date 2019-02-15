package knowingipr.dam.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import knowingipr.dam.model.DataSourceModel;

public class StatusBarController {

    @FXML
    public Label currentStatusLabel;

    private DataSourceModel model;

    public void initModel(DataSourceModel model) {
        this.model = model;

        currentStatusLabel.textProperty().bind(model.currentStatusProperty());
    }
}
