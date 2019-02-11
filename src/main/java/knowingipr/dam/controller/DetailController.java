package knowingipr.dam.controller;

import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
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
    public Button editButton;
    @FXML
    public Button saveButton;
    @FXML
    public Button discardButton;
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
                //sourceNameTextField.textProperty().unbindBidirectional(oldSource.nameProperty());
                //descriptionTextField.textProperty().unbindBidirectional(oldSource.descriptionProperty());
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
                updateIntervalTextField.setText("");
            } else {
                sourceNameTextField.setText(newSource.getName());
                descriptionTextField.setText(newSource.getDescription());
                urlTextField.setText(newSource.getUrl());
                schemeFileLabel.setText(newSource.getSchemaPath());
                mappingFileLabel.setText(newSource.getMappingPath());
                licenceFileLabel.setText(newSource.getLicencePath());
                licenceTypeTextField.setText(newSource.getLicenceType());
                dateLastUpdatedLabel.setText(newSource.getDateLastUpdated());
                categoryTypeTextField.setText(newSource.getCategoryType());
                updateIntervalTextField.setText(newSource.getUpdateIntervalDays()+"");



                /*sourceNameTextField.textProperty().bind(newSource.nameProperty());
                descriptionTextField.textProperty().bind(newSource.descriptionProperty());
                urlTextField.textProperty().bind(newSource.urlProperty());
                schemeFileLabel.textProperty().bind(newSource.schemaPathProperty());
                mappingFileLabel.textProperty().bind(newSource.mappingPathProperty());
                licenceFileLabel.textProperty().bind(newSource.licencePathProperty());
                licenceTypeTextField.textProperty().bind(newSource.licenceTypeProperty());
                dateLastUpdatedLabel.textProperty().bind(newSource.getDateLastUpdatedProperty());
                categoryTypeTextField.textProperty().bind(newSource.categoryTypeProperty());
                dateLastUpdatedLabel.textProperty().bind(newSource.getDateLastUpdatedProperty());
                updateIntervalTextField.textProperty().bind(Bindings.convert(newSource.updateIntervalDaysProperty()));*/
            }
        });
    }

    public void onSaveButtonClicked(ActionEvent actionEvent) {
        onDiscardButtonClicked(actionEvent);
    }

    public void onDiscardButtonClicked(ActionEvent actionEvent) {
        editButton.setDisable(false);
        saveButton.setVisible(false);
        discardButton.setVisible(false);

        sourceNameTextField.setDisable(true);
        descriptionTextField.setDisable(true);
        urlTextField.setDisable(true);
        licenceTypeTextField.setDisable(true);
        categoryTypeTextField.setDisable(true);
        updateIntervalTextField.setDisable(true);

        model.loadData();
    }

    public void onEditButtonClicked(ActionEvent actionEvent) {
        editButton.setDisable(true);
        saveButton.setVisible(true);
        discardButton.setVisible(true);

        sourceNameTextField.setDisable(false);
        descriptionTextField.setDisable(false);
        urlTextField.setDisable(false);
        licenceTypeTextField.setDisable(false);
        categoryTypeTextField.setDisable(false);
        updateIntervalTextField.setDisable(false);
    }
}
