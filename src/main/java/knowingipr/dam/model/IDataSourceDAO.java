package knowingipr.dam.model;

import java.util.List;

public interface IDataSourceDAO extends DAO {
    /**
     * Returns all the data sources from the database.
     * @return list of data sources
     */
    List<DataSource> findAll();

    /**
     * Finds a data source by id
     * @param id - id of the data source to find
     * @return - Found data source
     */
    DataSource findById(long id);

    /**
     * Inserts a new record to the database
     * @param dataSource - Data source to insert to the database
     * @return - An id of the inserted data source
     */
    Long insertDataSource(DataSource dataSource);

    /**
     * Deletes data source from the database
     * @param dataSource - data source to delete
     * @return - true if success, else false
     */
    boolean deleteDataSource(DataSource dataSource);

    /**
     * Updates a data source in the database
     * @param dataSource - data source containing updated data
     * @return - true if success, else false
     */
    boolean updateDataSource(DataSource dataSource);

    /**
     * Updates only a last updated date, which is not
     * updated when calling the <code>updateDataSource</code> method.
     * @param date - Date
     * @param id - Id of the data source to be updated
     * @return
     */
    boolean updateDate(String date, Long id);

    /**
     * Returns a list of category types from the database
     * @return - List of category types as strings
     */
    List<String> getCategoryTypes();
}
