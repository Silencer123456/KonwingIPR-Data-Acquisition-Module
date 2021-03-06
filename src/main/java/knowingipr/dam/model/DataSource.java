package knowingipr.dam.model;

import javafx.beans.property.*;

import java.util.Date;

/**
 * Domain object representing metadata for one source of data.
 */
public class DataSource {
    private final LongProperty id = new SimpleLongProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final StringProperty url = new SimpleStringProperty();
    private final StringProperty schemaPath = new SimpleStringProperty();
    private final StringProperty mappingPath = new SimpleStringProperty();
    private final StringProperty licenceType = new SimpleStringProperty();
    private final StringProperty licencePath = new SimpleStringProperty();
    private final StringProperty categoryType = new SimpleStringProperty();
    private StringProperty dateLastUpdatedString = new SimpleStringProperty();

    private final IntegerProperty updateIntervalDays = new SimpleIntegerProperty();

    private Date lastUpdatedDate;

    /**
     * Flag indicating if the current DataSource record is present in the source database.
     */
    private boolean inSourceDb;

    public DataSource(String name, String description, String url, String schemaPath, String mappingPath, String licenceType,
                      String licencePath, String categoryType, int updateInterval, Date dateLastUpdated) {
        setName(name);
        setDescription(description);
        setUrl(url);
        setSchemaPath(schemaPath);
        setMappingPath(mappingPath);
        setLicencePath(licencePath);
        setLicenceType(licenceType);
        setCategoryType(categoryType);
        setUpdateIntervalDays(updateInterval);
        setDateLastUpdatedString(dateLastUpdated.toString());
        lastUpdatedDate = dateLastUpdated;
    }

    /**
     * Constructor creating an empty data source instance.
     */
    public DataSource() {
        this("", "", "", "", "", "", "", "", 0, new Date());
    }

    public boolean isInSourceDb() {
        return inSourceDb;
    }

    public void setInSourceDb(boolean inSourceDb) {
        this.inSourceDb = inSourceDb;
    }


    public long getId() {
        return id.get();
    }

    public LongProperty idProperty() {
        return id;
    }

    public void setId(long id) {
        this.id.set(id);
    }


    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getDescription() {
        return description.get();
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public String getUrl() {
        return url.get();
    }

    public StringProperty urlProperty() {
        return url;
    }

    public void setUrl(String url) {
        this.url.set(url);
    }

    public String getSchemaPath() {
        return schemaPath.get();
    }

    public StringProperty schemaPathProperty() {
        return schemaPath;
    }

    public void setSchemaPath(String schemaPath) {
        this.schemaPath.set(schemaPath);
    }

    public String getMappingPath() {
        return mappingPath.get();
    }

    public StringProperty mappingPathProperty() {
        return mappingPath;
    }

    public void setMappingPath(String mappingPath) {
        this.mappingPath.set(mappingPath);
    }

    public String getLicenceType() {
        return licenceType.get();
    }

    public StringProperty licenceTypeProperty() {
        return licenceType;
    }

    public void setLicenceType(String licenceType) {
        this.licenceType.set(licenceType);
    }

    public String getLicencePath() {
        return licencePath.get();
    }

    public StringProperty licencePathProperty() {
        return licencePath;
    }

    public void setLicencePath(String licencePath) {
        this.licencePath.set(licencePath);
    }

    public String getCategoryType() {
        return categoryType.get();
    }

    public StringProperty categoryTypeProperty() {
        return categoryType;
    }

    public void setCategoryType(String categoryType) {
        this.categoryType.set(categoryType);
    }

    public int getUpdateIntervalDays() {
        return updateIntervalDays.get();
    }

    public IntegerProperty updateIntervalDaysProperty() {
        return updateIntervalDays;
    }

    public void setUpdateIntervalDays(int updateIntervalDays) {
        this.updateIntervalDays.set(updateIntervalDays);
    }

    public StringProperty getDateLastUpdatedProperty() {
        return dateLastUpdatedString;
    }

    public String getDateLastUpdatedString() {
        return dateLastUpdatedString.get();
    }

    public StringProperty dateLastUpdatedStringProperty() {
        return dateLastUpdatedString;
    }

    public void setDateLastUpdatedString(String dateLastUpdatedString) {
        this.dateLastUpdatedString.set(dateLastUpdatedString);
    }

    public Date getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public void setLastUpdatedDate(Date lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
        setDateLastUpdatedString(lastUpdatedDate.toString());
    }
}
