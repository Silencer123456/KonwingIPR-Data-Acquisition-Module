package knowingipr.dam.controller;

import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import knowingipr.dam.controller.ui.ListViewCell;
import knowingipr.dam.model.DataSource;
import knowingipr.dam.model.DataSourceModel;

/**
 * The controller handling user inputs from the list.fxml page
 */
public class ListController {

    @FXML
    public ListView<DataSource> listView;

    private DataSourceModel model;

    public void initModel(DataSourceModel model) {
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

        model.getSourcesList().addListener((ListChangeListener<DataSource>) c -> listView.getSelectionModel().select(0));

        listView.getSelectionModel().select(0);

        listView.setCellFactory(listView -> new ListViewCell(model));
    }

    public void onRefreshButtonClicked(ActionEvent actionEvent) {
        model.loadData();
    }

    /**
     * Resets the update interval for the selected data source item
     *
     * @param actionEvent
     */
    public void onResetIntervalButtonClicked(ActionEvent actionEvent) {
        if (model.extendExpiration()) {
            model.setCurrentStatus("Interval reset");
            model.loadData();
        } else {
            model.setCurrentStatus("Interval could not be reset");
        }
    }
}
