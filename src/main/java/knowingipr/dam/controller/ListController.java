package knowingipr.dam.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import knowingipr.dam.model.DataModel;
import knowingipr.dam.model.DataSource;

public class ListController {

    @FXML
    public ListView<DataSource> listView;

    private DataModel model;

    public void initModel(DataModel model) {
        if (this.model != null) {
            throw new IllegalStateException("Model can only be initialized once");
        }

        this.model = model;

        listView.setItems(model.getSourcesList());
        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) ->
                model.setCurrentSource(newSelection));
        model.currentSourceProperty().addListener((obs, oldSource, newSource) -> {
            if (newSource == null) {
                listView.getSelectionModel().clearSelection();
            } else {
                listView.getSelectionModel().select(newSource);
            }
        });

        listView.setCellFactory( lv -> new ListCell<DataSource>() {
            @Override
            protected void updateItem(DataSource source, boolean empty) {
                super.updateItem(source, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(source.getName() + " Description: " + source.getDescription());
                }
            }
        });
    }
}
