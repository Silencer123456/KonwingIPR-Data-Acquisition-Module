package knowingipr.dam.model;

import java.util.List;

public interface DataSourceDAO extends DAO {
    List<DataSource> findAll();
    Long insertDataSource(DataSource dataSource);
    List<String> getCategoryTypes();
}
