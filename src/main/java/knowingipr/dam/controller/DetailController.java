package knowingipr.dam.controller;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import knowingipr.dam.model.DataSource;
import knowingipr.dam.model.DataSourceModel;
import knowingipr.data.loader.*;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * The controller handling user inputs from the details.fxml page.
 */
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

    @FXML
    public Button schemeFileButton;

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
                dateLastUpdatedLabel.setText(newSource.getDateLastUpdatedString());
                updateIntervalTextField.setText(newSource.getUpdateIntervalDays() + "");
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

    /**
     * Toggles the editable value of the fields
     *
     * @param value = value to toggle the editable value to.
     */
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

    /**
     * Handles loading of data to the sources database
     *
     * @param actionEvent
     */
    public void onLoadCollectionButtonClicked(ActionEvent actionEvent) {
        if (sourceNameTextField.getText().equals("uspto")) {
            sourceDbLoader = new PatentLoader(dbConnection, mappingFileTextField.getText(), categoryTypeComboBox.getSelectionModel().getSelectedItem());
            doLoad(new String[]{"json"});
        } else if (sourceNameTextField.getText().equals("patstat")) {
            sourceDbLoader = new PatstatLoader(dbConnection, mappingFileTextField.getText(), categoryTypeComboBox.getSelectionModel().getSelectedItem());
            doLoad(new String[]{"json"});
        } else if (sourceNameTextField.getText().equals("mag")) {
            sourceDbLoader = new MagLoader(dbConnection, mappingFileTextField.getText(), categoryTypeComboBox.getSelectionModel().getSelectedItem());
            doLoad(new String[]{"txt"});
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING, "The parser for selected data source does not exist yet");
            alert.showAndWait();
        }
    }

    /**
     * Performs the loading of the data into the database.
     *
     * @param extensions - list of extensions to look for
     */
    private void doLoad(String[] extensions) {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                sourceDbLoader.loadFromDirectory(loadPathTextField.getText(), extensions);
                loadCollectionButton.setDisable(true);
                return null;
            }
        };
        new Thread(task).start();

        task.setOnSucceeded(evt -> {
            System.out.println(task.getValue());
            model.currentStatusProperty().setValue("Loading finished");
            loadCollectionButton.setDisable(false);
        });
        task.setOnFailed(evt -> {
            System.err.println("The task failed with the following exception:");
            model.currentStatusProperty().setValue(task.getException().getMessage());
            task.getException().printStackTrace(System.err);
            loadCollectionButton.setDisable(false);
        });
    }

    /**
     * Handles saving of the data source item. If the
     * data source is newly created, new record is inserted to
     * the database, else the record is updated.
     *
     * @param actionEvent
     */
    public void onSaveButtonClicked(ActionEvent actionEvent) {

        boolean valid = validateFields();

        if (!valid) {
            return;
        }

        int updateInterval = Integer.parseInt(updateIntervalTextField.getText());

        // Editing newly created record
        if (model.getCurrentSource().getId() == 0) {
            model.addNewDataSource(sourceNameTextField.getText(), descriptionTextField.getText(), urlTextField.getText(),
                    schemeFileTextField.getText(), mappingFileTextField.getText(), licenceTypeTextField.getText(), licenceFileTextField.getText(),
                    categoryTypeComboBox.getSelectionModel().getSelectedItem(), updateInterval);

        }
        // Editing existing record, only update
        else {
            DataSource dataSource = new DataSource();
            dataSource.setName(sourceNameTextField.getText());
            dataSource.setDescription(descriptionTextField.getText());
            dataSource.setUrl(urlTextField.getText());
            dataSource.setSchemaPath(schemeFileTextField.getText());
            dataSource.setMappingPath(mappingFileTextField.getText());
            dataSource.setLicenceType(licenceTypeTextField.getText());
            dataSource.setLicencePath(licenceFileTextField.getText());
            dataSource.setCategoryType(categoryTypeComboBox.getSelectionModel().getSelectedItem());
            dataSource.setUpdateIntervalDays(updateInterval);
            dataSource.setId(model.getCurrentSource().getId());
            if (model.updateDataSource(dataSource)) {
                model.currentStatusProperty().setValue("Record updated");
            } else {
                model.currentStatusProperty().setValue("There was an error updating the record");
            }
        }

        toggleEditMode(false);

        model.loadData();
    }

    /**
     * Validates the form
     *
     * @return - true if valid, else false
     */
    private boolean validateFields() {
        int updateInterval;
        try {
            updateInterval = Integer.parseInt(updateIntervalTextField.getText());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "The update interval is not a number.");
            return false;
        }

        if (updateIntervalTextField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "The update interval cannot be empty.");
            return false;
        }

        if (sourceNameTextField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Source name cannot be empty.");
            return false;
        }
        if (mappingFileTextField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Mapping file name cannot be empty.");
            return false;
        }

        return true;
    }

    /**
     * Disables the edit mode
     *
     * @param actionEvent
     */
    public void onDiscardButtonClicked(ActionEvent actionEvent) {
        toggleEditMode(false);

        model.loadData();
    }

    /**
     * Toggles the activeness of the edit mode, where the user can edit
     * the text fields.
     *
     * @param value - value indicating whether to activate the edit mode
     */
    private void toggleEditMode(boolean value) {
        editButton.setDisable(value);
        saveButton.setVisible(value);
        discardButton.setVisible(value);

        categoryTypeComboBox.setDisable(!value);

        toggleEditableTextFields(value);
    }

    public void onEditButtonClicked(ActionEvent actionEvent) {
        toggleEditMode(true);
    }

    /**
     * Handles creation of new data source item
     *
     * @param actionEvent
     */
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
     *
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
     *
     * @param textField - Textfield to be set after the user chooses the file.
     */
    private void showFileChooserAndSet(TextField textField) {
        Stage stage = (Stage) textField.getScene().getWindow();
        DirectoryChooser dirChooser = new DirectoryChooser();
        File selectedDir = dirChooser.showDialog(stage);
        if (selectedDir != null) {
            try {
                textField.setText(selectedDir.getCanonicalPath());
            } catch (IOException e) {
                model.setCurrentStatus("Selected directory does not exist.");
            }
        } else {
            model.setCurrentStatus("Directory selection cancelled.");
        }
    }

    /**
     * Shows an alert to the user and waits for his response
     *
     * @param alertType - The type of alert
     * @param content   - The text to be displayed in the alert
     */
    private void showAlert(Alert.AlertType alertType, String content) {
        Alert alert = new Alert(alertType, content);
        alert.setTitle("Alert");
        alert.showAndWait();
    }

    /**
     * Handles deletion of the record
     *
     * @param actionEvent
     */
    public void onDeleteButtonClicked(ActionEvent actionEvent) {
        boolean value = model.deleteDataSource(model.getCurrentSource().getId());
        if (!value) {
            model.setCurrentStatus("Could not delete");
        }
        model.loadData();
    }

    /**
     * Shows file chooser and sets the result to the text field
     *
     * @param actionEvent
     */
    public void onOpenLoadButtonClicked(ActionEvent actionEvent) {
        showFileChooserAndSet(loadPathTextField);
    }

    /**
     * Handles opening of the URL in the browser
     *
     * @param actionEvent
     */
    public void onOpenLinkButtonClicked(ActionEvent actionEvent) {
        try {
            Desktop.getDesktop().browse(new URI(urlTextField.getText()));
        } catch (IOException | URISyntaxException e1) {
            model.setCurrentStatus("Wrong URI");
        }
    }
}
