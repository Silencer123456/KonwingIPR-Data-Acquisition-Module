package knowingipr.dam.model;

import java.util.List;

/**
 * Abstract data accessor. Concrete implementations can load from filesystem, database...
 */
public interface DataSourceDataAccessor {

    /**
     * Loads the <code>DataSource</code> objects from the data source.
     * @return - list of DataSource objects fetched from the concrete data source
     */
    List<DataSource> load();
}
