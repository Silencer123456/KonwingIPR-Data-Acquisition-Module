package knowingipr.dam.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import knowingipr.dam.model.DataSource;
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
    public TextField licenceFileTextField;
    @FXML
    public TextField schemeFileTextField;
    @FXML
    public TextField mappingFileTextField;
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
                schemeFileTextField.setText("");
                mappingFileTextField.setText("");
                licenceFileTextField.setText("");
                licenceTypeTextField.setText("");
                dateLastUpdatedLabel.setText("");
                updateIntervalTextField.setText("");
                toggleEditMode(false);
            } else {
                sourceNameTextField.setText(newSource.getName());
                descriptionTextField.setText(newSource.getDescription());
                urlTextField.setText(newSource.getUrl());
                schemeFileTextField.setText(newSource.getSchemaPath());
                mappingFileTextField.setText(newSource.getMappingPath());
                licenceFileTextField.setText(newSource.getLicencePath());
                licenceTypeTextField.setText(newSource.getLicenceType());
                dateLastUpdatedLabel.setText(newSource.getDateLastUpdated());
                updateIntervalTextField.setText(newSource.getUpdateIntervalDays()+"");
                categoryTypeComboBox.getSelectionModel().select(newSource.getCategoryType());
                toggleEditMode(false);
            }
        });

        categoryTypeComboBox.setItems(model.getCategoryTypesList());

        model.currentSourceProperty().addListener((obs, oldSource, newSource) -> {
            if (newSource == null) {
                editButton.setVisible(false);
            } else {
                editButton.setVisible(true);
            }
        });
    }

    private void toggleEditableTextFields(boolean value) {
        sourceNameTextField.setEditable(value);
        descriptionTextField.setEditable(value);
        urlTextField.setEditable(value);
        licenceTypeTextField.setEditable(value);
        updateIntervalTextField.setEditable(value);
        mappingFileTextField.setEditable(value);
        licenceFileTextField.setEditable(value);
        schemeFileTextField.setEditable(value);
    }

    public void onLoadCollectionButtonClicked(ActionEvent actionEvent) {
        if (sourceNameTextField.getText().equals("uspto")) {
            sourceDbLoader = new PatentLoader(dbConnection, mappingFileTextField.getText(), categoryTypeComboBox.getSelectionModel().getSelectedItem());
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

    public void onSaveButtonClicked(ActionEvent actionEvent) {
        int updateInterval;
        try {
            updateInterval = Integer.parseInt(updateIntervalTextField.getText());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "The update interval is not a number.");
            return;
        }

        model.addNewDataSource(sourceNameTextField.getText(), descriptionTextField.getText(), urlTextField.getText(),
                schemeFileTextField.getText(), mappingFileTextField.getText(), licenceTypeTextField.getText(), licenceFileTextField.getText(),
                categoryTypeComboBox.getSelectionModel().getSelectedItem(), updateInterval);
        toggleEditMode(false);

        model.loadData();
    }

    public void onDiscardButtonClicked(ActionEvent actionEvent) {
        toggleEditMode(false);

        model.loadData();
    }

    /**
     * Toggles the activeness of the edit mode, where the user can edit
     * the text fields.
     * @param value - value indicating whether to activate the edit mode
     */
    private void toggleEditMode(boolean value) {
        editButton.setDisable(value);
        saveButton.setVisible(value);
        discardButton.setVisible(value);

        toggleEditableTextFields(value);
    }

    public void onEditButtonClicked(ActionEvent actionEvent) {
        toggleEditMode(true);
    }

    public void onAddNewButtonClicked(ActionEvent actionEvent) {
        DataSource empty = new DataSource();
        model.getSourcesList().add(empty);
        model.setCurrentSource(empty);
        onEditButtonClicked(actionEvent);
    }

    public void onOpenSchemeFileButton(ActionEvent actionEvent) {
        openDesktopPath(schemeFileTextField.getText());
    }

    public void onOpenMappingFileButton(ActionEvent actionEvent) {
        openDesktopPath(mappingFileTextField.getText());
    }

    public void onOpenLicenceFileButton(ActionEvent actionEvent) {
        openDesktopPath(licenceFileTextField.getText());
    }

    /**
     * Attempts to open a file from desktop environment.
     * If the path does not exist, displays an error alert to the user.
     * @param path - Path to the file or folder to be opened
     */
    private void openDesktopPath(String path) {
        try {
            File f = new File(path);
            Desktop.getDesktop().open(f);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Path does not exist: " + e.getMessage());
        }
    }

    /**
     * Displays a File Chooser to the user. After the user selected the requested file,
     * the textField is set to its path.
     * @param textField - Textfield to be set after the user chooses the file.
     */
    private void showFileChooserAndSet(TextField textField) {
        Stage stage = (Stage) textField.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            try {
                textField.setText("File selected: " + selectedFile.getCanonicalPath());
            } catch (IOException e) {
                mappingFileTextField.setText("Selected file does not exist.");
            }
        }
        else {
            mappingFileTextField.setText("File selection cancelled.");
        }
    }

    /**
     * Shows an alert to the user and waits for his response
     * @param alertType - The type of alert
     * @param content - The text to be displayed in the alert
     */
    private void showAlert(Alert.AlertType alertType, String content) {
        Alert alert = new Alert(alertType, content);
        alert.showAndWait();
    }

    public void onDeleteButtonClicked(ActionEvent actionEvent) {
        boolean value = model.deleteDataSource(model.getCurrentSource().getId());
        model.loadData();
    }
}
