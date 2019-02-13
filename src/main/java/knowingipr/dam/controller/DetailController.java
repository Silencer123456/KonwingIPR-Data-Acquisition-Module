package knowingipr.dam.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import knowingipr.dam.model.DataSourceModel;
import knowingipr.data.loader.MongoDbConnection;
import knowingipr.data.loader.PatentLoader;
import knowingipr.data.loader.SourceDbConnection;
import knowingipr.data.loader.SourceDbLoader;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class DetailController {

    @FXML
    public TextField urlTextField;
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
    public ComboBox<String> categoryTypeComboBox;
    @FXML
    public TextField loadPathTextField;
    @FXML
    public Button loadCollectionButton;
    @FXML
    private TextField sourceNameTextField;
    @FXML
    private TextField descriptionTextField;

    private DataSourceModel model;

    private SourceDbLoader sourceDbLoader;
    SourceDbConnection dbConnection = new MongoDbConnection();

    public void initModel(DataSourceModel model) {
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
                updateIntervalTextField.setText(newSource.getUpdateIntervalDays()+"");
                categoryTypeComboBox.getSelectionModel().select(newSource.getCategoryType());

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

        categoryTypeComboBox.setItems(model.getCategoryTypesList());
    }

    public void onSaveButtonClicked(ActionEvent actionEvent) {
        onDiscardButtonClicked(actionEvent);
    }

    public void onDiscardButtonClicked(ActionEvent actionEvent) {
        editButton.setDisable(false);
        saveButton.setVisible(false);
        discardButton.setVisible(false);

        toggleEditable(false);

        model.loadData();
    }

    public void onEditButtonClicked(ActionEvent actionEvent) {
        editButton.setDisable(true);
        saveButton.setVisible(true);
        discardButton.setVisible(true);

        toggleEditable(true);
    }

    private void toggleEditable(boolean value) {
        sourceNameTextField.setEditable(value);
        descriptionTextField.setEditable(value);
        urlTextField.setEditable(value);
        licenceTypeTextField.setEditable(value);
        updateIntervalTextField.setEditable(value);
    }

    public void onOpenSchemeFileButton(ActionEvent actionEvent) {
        try {
            /*String path = schemeFileLabel.getText();
            File file = new File(path);
            if (!file.isAbsolute()) {
                path = System.getProperty("user.dir") + path;
            }*/

            Desktop.getDesktop().open(new File(schemeFileLabel.getText()));
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Path does not exist: " + e.getMessage());
            alert.showAndWait();
            e.printStackTrace();
        }
    }

    public void onLoadCollectionButtonClicked(ActionEvent actionEvent) {
        if (sourceNameTextField.getText().equals("uspto")) {
            sourceDbLoader = new PatentLoader(dbConnection, mappingFileLabel.getText(), categoryTypeComboBox.getSelectionModel().getSelectedItem());
            try {
                sourceDbLoader.loadFromDirectory(loadPathTextField.getText(), new String[]{"json"});
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            Alert alert = new Alert(Alert.AlertType.WARNING, "The parser for selected data source does not exist yet");
            alert.showAndWait();
        }
    }
}
