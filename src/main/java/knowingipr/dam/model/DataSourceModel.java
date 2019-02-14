package knowingipr.dam.model;

import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

/**
 * A model class providing access to the repository for Data Sources.
 */
public class DataSourceModel {

   private IDataSourceDAO dataSourceDAO;

    public DataSourceModel(IDataSourceDAO dataSourceDAO) {
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

    private final ObjectProperty<DataSource> currentSource = new SimpleObjectProperty<>(null);

    public ObservableList<String> getCategoryTypesList() {
        return categoryTypesList;
    }

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

    public void loadCategoryTypes() {
        categoryTypesList.setAll(dataSourceDAO.getCategoryTypes());
    }

    public void addNewDataSource(String name, String description, String url, String schemaPath, String mappingPath, String licenceType,
                                 String licencePath, String categoryType, int updateInterval, String dateLastUpdated) {
        DataSource dataSource = new DataSource(name, description, url, schemaPath, mappingPath, licenceType, licencePath,
                categoryType, updateInterval, dateLastUpdated);
        dataSourceDAO.insertDataSource(dataSource);
    }

    public boolean deleteDataSource(long id) {
        DataSource dataSource = getDataSourceWithId(id);
        if (dataSource != null) {
            return dataSourceDAO.deleteDataSource(dataSource);
        }

        return false;
    }

    // TODO: CHANGE TO FETCH FROM DATABASE INSTEAD
    private DataSource getDataSourceWithId(long id) {
        for (DataSource dataSource : getSourcesList()) {
            if (dataSource.getId() == id) {
                return dataSource;
            }
        }

        return null;
    }

    public void loadData() {
        List<DataSource> loadedSources = dataSourceDAO.findAll();
        sourcesList.setAll(loadedSources);
        // mock data
        /*sourcesList.setAll(
                new DataSource("test1", "this is test 1"),
                new DataSource("test2", "this is test 2"),
                new DataSource("test3", "this is test 3")
        );*/
    }
}
