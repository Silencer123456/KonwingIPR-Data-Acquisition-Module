package knowingipr.dam.model;

import com.mongodb.client.MongoDatabase;
import knowingipr.dam.database.DbManager;
import knowingipr.dam.database.SourceDbManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Provides access for the DataSource to the database.
 */
public class DataSourceDbAccessor implements DataSourceDataAccessor {

    private Connection connection;

    public DataSourceDbAccessor() {
        DbManager dbManager = new DbManager();
        try {
            connection = dbManager.createConnection();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<DataSource> load() {
        List<DataSource> sourcesList = new ArrayList<>();
        try (
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("select * from sources"); // TODO: Edit
        ) {

            while (resultSet.next()) {
                String name = resultSet.getString("name");
                String url = resultSet.getString("url");
                String description = resultSet.getString("description");
                String schemaPath = resultSet.getString("schemaPath");
                String mappingPath = resultSet.getString("mappingFilePath");
                String licenceType = resultSet.getString("licenceType");
                String licenceFilePath = resultSet.getString("licenceFilePath");
                String categoryType = resultSet.getString("categoryType");
                int updateInterval = resultSet.getInt("updateIntervalDays");
                Date dateLastUpdated = resultSet.getDate("dateLastUpdated");

                DataSource dataSource = new DataSource(name, description, url, schemaPath, mappingPath, licenceType, licenceFilePath, categoryType, updateInterval, dateLastUpdated.toString());

                // TODO: Move to data model
                /*SourceDbManager sourceDbManager = new SourceDbManager();

                MongoDatabase mongoDatabase = sourceDbManager.getMongoDatabase();*/

                sourcesList.add(dataSource);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return sourcesList;
    }
}
