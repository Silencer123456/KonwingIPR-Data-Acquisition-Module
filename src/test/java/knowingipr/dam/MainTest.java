package knowingipr.dam;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import knowingipr.dam.model.DataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.loadui.testfx.GuiTest;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit.ApplicationTest;
import knowingipr.dam.controller.DetailController;
import knowingipr.dam.controller.ListController;
import knowingipr.dam.controller.StatusBarController;
import knowingipr.dam.controller.ToolsController;
import knowingipr.dam.model.DataSourceDAO;
import knowingipr.dam.model.DataSourceModel;
import knowingipr.dam.model.IDataSourceDAO;

import java.util.Date;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class MainTest extends ApplicationTest {

    private DataSourceModel model;

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        BorderPane centerPane = new BorderPane();
        root.setCenter(centerPane);

        FXMLLoader listLoader = new FXMLLoader(getClass().getResource("/list.fxml"));
        centerPane.setCenter(listLoader.load());
        ListController listController = listLoader.getController();

        FXMLLoader toolLoader = new FXMLLoader(getClass().getResource("/tools.fxml"));
        centerPane.setBottom(toolLoader.load());
        ToolsController toolsController = toolLoader.getController();

        FXMLLoader detailLoader = new FXMLLoader(getClass().getResource("/detail.fxml"));
        root.setRight(detailLoader.load());
        DetailController detailController = detailLoader.getController();

        FXMLLoader statusBarLoader = new FXMLLoader(getClass().getResource("/statusBar.fxml"));
        root.setBottom(statusBarLoader.load());
        StatusBarController statusBarController = statusBarLoader.getController();

        IDataSourceDAO sourceDAO = new DataSourceDAO();

        model = new DataSourceModel(sourceDAO);
        detailController.initModel(model);
        listController.initModel(model);
        statusBarController.initModel(model);
        toolsController.initModel(model);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Data Administrator");
        primaryStage.show();
    }

    /**
     * Tests if the fields are editable after clicking the edit button.
     * Also tests if the appropriate buttons are enabled and visible.
     */
    @Test
    public void areFieldsEditableAfterEditButtonClicked() {
        Button editButton = GuiTest.find("#editButton");
        Button deleteButton = GuiTest.find("#deleteButton");

        TextField schemeFileTextField = GuiTest.find("#schemeFileTextField");
        TextField sourceNameTextField = GuiTest.find("#sourceNameTextField");
        TextField descriptionTextField = GuiTest.find("#descriptionTextField");
        TextField urlTextField = GuiTest.find("#urlTextField");
        TextField licenceTypeTextField = GuiTest.find("#licenceTypeTextField");
        TextField updateIntervalTextField = GuiTest.find("#updateIntervalTextField");
        TextField mappingFileTextField = GuiTest.find("#mappingFileTextField");
        TextField licenceFileTextField = GuiTest.find("#licenceFileTextField");
        TextField loadPathTextField = GuiTest.find("#loadPathTextField");

        assertFalse(deleteButton.isDisabled());

        clickOn("#editButton");

        Button saveButton = GuiTest.find("#saveButton");
        Button discardButton = GuiTest.find("#discardButton");

        assertTrue(schemeFileTextField.isEditable());
        assertTrue(sourceNameTextField.isEditable());
        assertTrue(descriptionTextField.isEditable());
        assertTrue(urlTextField.isEditable());
        assertTrue(licenceTypeTextField.isEditable());
        assertTrue(updateIntervalTextField.isEditable());
        assertTrue(mappingFileTextField.isEditable());
        assertTrue(licenceFileTextField.isEditable());
        assertTrue(loadPathTextField.isEditable());

        assertFalse(deleteButton.isDisabled());
        assertTrue(saveButton.isVisible());
        assertFalse(saveButton.isDisabled());

        assertTrue(discardButton.isVisible());
        assertFalse(discardButton.isDisabled());

        assertTrue(editButton.isDisabled());
    }

    /**
     * Tests the insertion to the database
     */
    @Test
    public void testDatabaseInsertRecord() {
        model.loadData();
        int sourcesCount = model.getSourcesList().size();

        model.addNewDataSource("test", "test", "test", "test",
                "test", "test","test","test", 7);
        model.loadData();
        int newCount = model.getSourcesList().size();

        assertEquals(sourcesCount + 1, newCount);
    }

    /**
     * Test adds a dummy record to the database and then deletes it. Tests the insertion and deletion of
     * a record from the database.
     */
    @Test
    public void testDatabaseRemoveRecord() {
        model.loadData();
        int originalCount = model.getSourcesList().size();

        model.addNewDataSource("test", "test", "test", "test",
                "test", "test","test","test", 7);
        model.loadData();

        List<DataSource> sources = model.getSourcesList();
        DataSource insertedSource = null;
        for (DataSource source : sources) {
            if (source.getName().equals("test")) {
                insertedSource = source;
            }
        }

        assertNotNull(insertedSource);

        long id = insertedSource.getId();
        model.deleteDataSource(id);
        model.loadData();

        int newCount = model.getSourcesList().size();
        assertEquals(originalCount, newCount);
    }
}