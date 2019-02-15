package knowingipr.dam.controller.ui;

import javafx.scene.control.ListCell;
import knowingipr.dam.model.DataSource;
import knowingipr.dam.model.DataSourceModel;

public class ListViewCell extends ListCell<DataSource> {

    private DataSourceModel model;
    private ListViewData data;

    public ListViewCell(DataSourceModel model) {
        this.model = model;
        data = new ListViewData(model);
    }

    @Override
    protected void updateItem(DataSource item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            //setText(item.getName() + " collection: " + item.getCategoryType() + " Expired: " + model.isSourceExpired(item));
            data.setData(item);
            setGraphic(data.getBox());
        }
    }
}
