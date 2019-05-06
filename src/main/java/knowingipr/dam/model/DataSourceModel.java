package knowingipr.dam.model;

import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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

    private final ObservableList<DataSource> sourcesList = FXCollections.observableArrayList(source ->
            new Observable[]{source.nameProperty(), source.descriptionProperty()});

    private final ObservableList<String> categoryTypesList = FXCollections.observableArrayList();

    private final ObjectProperty<DataSource> currentSource = new SimpleObjectProperty<>(null);

    private final StringProperty currentStatus = new SimpleStringProperty();

    /**
     * Loads types of categories from the database
     */
    public void loadCategoryTypes() {
        categoryTypesList.setAll(dataSourceDAO.getCategoryTypes());
    }

    public void addNewDataSource(String name, String description, String url, String schemaPath, String mappingPath, String licenceType,
                                 String licencePath, String categoryType, int updateInterval) {
        DataSource dataSource = new DataSource(name, description, url, schemaPath, mappingPath, licenceType, licencePath,
                categoryType, updateInterval, new Date());
        dataSourceDAO.insertDataSource(dataSource);
    }

    /**
     * Deletes data source from the database by its id
     *
     * @param id - id of data source to delete
     * @return true if success, else false
     */
    public boolean deleteDataSource(long id) {
        DataSource dataSource = getDataSourceWithId(id);
        if (dataSource != null) {
            return dataSourceDAO.deleteDataSource(dataSource);
        }

        return false;
    }

    /**
     * Updates a specified data source
     *
     * @param dataSource - data source to update
     * @return - true if successful, else false
     */
    public boolean updateDataSource(DataSource dataSource) {
        return dataSourceDAO.updateDataSource(dataSource);
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

    /**
     * Loads all the data sources from the db.
     */
    public void loadData() {
        List<DataSource> loadedSources = dataSourceDAO.findAll();
        sourcesList.setAll(loadedSources);
    }

    /**
     * Calculates if the source is expired and should be updated
     *
     * @return - true if the source is expired, else false
     */
    public boolean isSourceExpired(DataSource dataSource) {
        Date lastUpdateDate = dataSource.getLastUpdatedDate();
        Calendar expirationCal = Calendar.getInstance();
        expirationCal.setTime(lastUpdateDate);

        int updateInterval = dataSource.getUpdateIntervalDays();
        expirationCal.add(Calendar.DAY_OF_MONTH, updateInterval);

        Date today = java.util.Calendar.getInstance().getTime();
        Calendar todayCal = Calendar.getInstance();
        todayCal.setTime(today);

        return expirationCal.getTime().before(todayCal.getTime());
    }

    /**
     * Extends the expiration of the data source by
     * setting the last updated date to today.
     *
     * @return true if success, else false
     */
    public boolean extendExpiration() {
        if (getCurrentSource() == null) {
            return false;
        }
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        String d = dateFormat.format(date);
        return dataSourceDAO.updateDate(d, getCurrentSource().getId());
    }

    public ObservableList<String> getCategoryTypesList() {
        return categoryTypesList;
    }

    public ObjectProperty<DataSource> currentSourceProperty() {
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

    public String getCurrentStatus() {
        return currentStatus.get();
    }

    public StringProperty currentStatusProperty() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus.set(currentStatus);
    }
}
