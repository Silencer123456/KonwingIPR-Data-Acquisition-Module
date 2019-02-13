package knowingipr.dam.model;

import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * A model class providing access to the repository for Data Sources.
 */
public class DataSourceModel {

   private DataSourceDAO dataSourceDAO;

    public DataSourceModel(DataSourceDAO dataSourceDAO) {
        this.dataSourceDAO = dataSourceDAO;
        try {
            dataSourceDAO.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        loadData();
        loadCategoryTypes();
    }

    private final ObservableList<DataSource> sourcesList = FXCollections.observableArrayList( source ->
            new Observable[] {source.nameProperty(), source.descriptionProperty()});

    private final ObservableList<String> categoryTypesList = FXCollections.observableArrayList();

    public ObservableList<String> getCategoryTypesList() {
        return categoryTypesList;
    }

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

    public void loadCategoryTypes() {
        categoryTypesList.setAll(dataSourceDAO.getCategoryTypes());
    }

    public void loadData() {
        sourcesList.setAll(dataSourceDAO.findAll());
        // mock data
        /*sourcesList.setAll(
                new DataSource("test1", "this is test 1"),
                new DataSource("test2", "this is test 2"),
                new DataSource("test3", "this is test 3")
        );*/
    }
}
