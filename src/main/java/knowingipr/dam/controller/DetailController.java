package knowingipr.dam.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import knowingipr.dam.model.DataModel;

public class DetailController {

    @FXML
    public TextField urlTextField;
    @FXML
    public TextField categoryTypeTextField;
    @FXML
    public TextField licenceTypeTextField;
    @FXML
    public Label schemeFileLabel;
    @FXML
    public Label mappingFileLabel;
    @FXML
    public Label licenceFileLabel;
    @FXML
    public TextField updateIntervalTextField;
    @FXML
    public Label dateLastUpdatedLabel;
    @FXML
    private TextField sourceNameTextField;
    @FXML
    private TextField descriptionTextField;

    private DataModel model;

    public void initModel(DataModel model) {
        if (this.model != null) {
            throw new IllegalStateException("Model can only be initialized once");
        }

        this.model = model;

        model.currentSourceProperty().addListener((obs, oldSource, newSource) -> {
            if (oldSource != null) {
                sourceNameTextField.textProperty().unbindBidirectional(oldSource.nameProperty());
                descriptionTextField.textProperty().unbindBidirectional(oldSource.descriptionProperty());
            }
            if (newSource == null) {
                sourceNameTextField.setText("");
                descriptionTextField.setText("");
                urlTextField.setText("");
                schemeFileLabel.setText("");
                mappingFileLabel.setText("");
                licenceFileLabel.setText("");
                licenceTypeTextField.setText("");
                dateLastUpdatedLabel.setText("");
                categoryTypeTextField.setText("");
            } else {
                sourceNameTextField.textProperty().bind(newSource.nameProperty());
                descriptionTextField.textProperty().bind(newSource.descriptionProperty());
                urlTextField.textProperty().bind(newSource.urlProperty());
                schemeFileLabel.textProperty().bind(newSource.schemaPathProperty());
                mappingFileLabel.textProperty().bind(newSource.mappingPathProperty());
                licenceFileLabel.textProperty().bind(newSource.licencePathProperty());
                licenceTypeTextField.textProperty().bind(newSource.licenceTypeProperty());
                dateLastUpdatedLabel.textProperty().bind(newSource.getDateLastUpdated());
                categoryTypeTextField.textProperty().bind(newSource.categoryTypeProperty());
            }
        });
    }
}
