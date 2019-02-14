package knowingipr.dam.model;

import java.util.List;

public interface IDataSourceDAO extends DAO {
    List<DataSource> findAll();
    DataSource findById(long id);
    Long insertDataSource(DataSource dataSource);
    boolean deleteDataSource(DataSource dataSource);
    List<String> getCategoryTypes();
}
