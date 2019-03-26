package knowingipr.dam.model;


import knowingipr.dam.database.DbManager;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class DataSourceDAO implements IDataSourceDAO {

    private Connection connection;

    private QueryRunner dbAccess = new QueryRunner();

    @Override
    public List<DataSource> findAll() {
        String query = "SELECT sources.*, category_type.name AS categoryName " +
                "FROM sources " +
                "INNER JOIN category_type\n" +
                "\tWHERE sources.categoryTypeId=category_type.categoryTypeId";

        List<DataSource> sourcesList = new ArrayList<>();
        try (
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query); // TODO: Edit
        ) {
            while (resultSet.next()) {
                int id = resultSet.getInt("sourceId");
                String name = resultSet.getString("name");
                String url = resultSet.getString("url");
                String description = resultSet.getString("description");
                String schemaPath = resultSet.getString("schemaPath");
                String mappingPath = resultSet.getString("mappingFilePath");
                String licenceType = resultSet.getString("licenceType");
                String licenceFilePath = resultSet.getString("licenceFilePath");
                String categoryType = resultSet.getString("categoryName");
                int updateInterval = resultSet.getInt("updateIntervalDays");
                Date dateLastUpdated = resultSet.getDate("dateLastUpdated");

                DataSource dataSource = new DataSource(name, description, url, schemaPath, mappingPath, licenceType, licenceFilePath, categoryType, updateInterval, dateLastUpdated);
                dataSource.setId(id);

                // TODO: Move to data model
                /*MongoDbConnection sourceDbManager = new MongoDbConnection();

                MongoDatabase mongoDatabase = sourceDbManager.getMongoDatabase();*/

                sourcesList.add(dataSource);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return sourcesList;
    }

    // TODO: finish
    @Override
    public DataSource findById(long id) {
        String query = "SELECT sources.*, category_type.name AS categoryName " +
                "FROM sources " +
                "INNER JOIN category_type\n" +
                "\tWHERE sources.categoryTypeId=category_type.categoryTypeId " +
                "AND sources.sourceId = " + id;
        return null;
    }

    @Override
    public Long insertDataSource(DataSource dataSource) {
        try {
            long id = dbAccess.insert(connection, "INSERT INTO sources\n" +
                            "\t(name, url, description, updateIntervalDays, schemaPath, mappingFilePath, licenceType, licenceFilePath, categoryTypeId)\n" +
                            "\tVALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    new ScalarHandler<>(), dataSource.getName(), dataSource.getUrl(), dataSource.getDescription(), dataSource.getUpdateIntervalDays(),
                    dataSource.getSchemaPath(), dataSource.getMappingPath(), dataSource.getLicenceType(), dataSource.getLicencePath(), 1); // TODO: change category type
            return id;
        } catch (SQLException e) {
            e.printStackTrace(); //TODO: Throw exception up
        }

        return -1L;
    }

    @Override
    public boolean deleteDataSource(DataSource dataSource) {
        try {
            dbAccess.update(connection, "DELETE FROM sources WHERE sourceId=?", dataSource.getId());
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean updateDataSource(DataSource dataSource) {
        String sql = "UPDATE sources SET name = " + dataSource.getName() +
                ", url = " + dataSource.getUrl() +
                ", description = " + dataSource.getDescription() +
                ", schemaPath = " + dataSource.getSchemaPath() +
                ", mappingFilePath = " + dataSource.getMappingPath() +
                ", licenceType = " + dataSource.getLicenceType() +
                ", licenceFilePath = " + dataSource.getLicencePath() +
                ", updateIntervalDays = " + dataSource.getUpdateIntervalDays() +
                ", dateLastUpdated = " + dataSource.getDateLastUpdatedString() +
                " WHERE sourceId = " + dataSource.getId();

        try {
            dbAccess.update(connection,
                    "UPDATE sources SET name = ?, " +
                            "url = ?, " +
                            "description = ?, " +
                            "schemaPath = ?, " +
                            "mappingFilePath = ?, " +
                            "licenceType = ?, " +
                            "licenceFilePath = ?, " +
                            "updateIntervalDays = ? " +
                            "WHERE sourceId = ?", dataSource.getName(), dataSource.getUrl(), dataSource.getDescription(),
                    dataSource.getSchemaPath(), dataSource.getMappingPath(), dataSource.getLicenceType(),
                    dataSource.getLicencePath(), dataSource.getUpdateIntervalDays(), dataSource.getId());

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }


    @Override
    public List<String> getCategoryTypes() {
        String query = "SELECT name " +
                "FROM category_type";
        try {
            return dbAccess.query(connection, query,
                    new ColumnListHandler<>());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }

    @Override
    public void setup() throws Exception {
    }

    @Override
    public void connect() throws Exception {
        DbManager dbManager = new DbManager();
        connection = dbManager.createConnection();
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }
}
