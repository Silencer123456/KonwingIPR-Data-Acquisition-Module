package knowingipr.dam.model;

import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public class DataModel {

    private DataSourceDataAccessor dataAccessor;

    public DataModel(DataSourceDataAccessor dataAccessor) {
        this.dataAccessor = dataAccessor;
        loadData();
    }

    private final ObservableList<DataSource> sourcesList = FXCollections.observableArrayList( source ->
            new Observable[] {source.nameProperty(), source.descriptionProperty()});

    private final ObjectProperty<DataSource> currentSource = new SimpleObjectProperty<>(null);

    public ObjectProperty<DataSource> getCurrentSourceProperty() {
        return currentSource;
    }

    public DataSource getCurrentSource() {
        return currentSource.get();
    }

    public void setCurrentSource(DataSource currentSource) {
        this.currentSource.set(currentSource);
    }

    public ObservableList<DataSource> getSourcesList() {
        return sourcesList;
    }

    public ObjectProperty<DataSource> currentSourceProperty() {
        return currentSource;
    }

    public void loadData() {
        sourcesList.setAll(dataAccessor.load());
        // mock data
        /*sourcesList.setAll(
                new DataSource("test1", "this is test 1"),
                new DataSource("test2", "this is test 2"),
                new DataSource("test3", "this is test 3")
        );*/
    }
}
