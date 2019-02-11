package knowingipr.dam.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import knowingipr.dam.model.DataModel;

public class DetailController {

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
            } else {
                sourceNameTextField.textProperty().bindBidirectional(newSource.nameProperty());
                descriptionTextField.textProperty().bindBidirectional(newSource.descriptionProperty());
            }
        });
    }
}
