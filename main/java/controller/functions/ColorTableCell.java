package controller.functions;

import javafx.scene.control.ColorPicker;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.paint.Color;

public class ColorTableCell<Container> extends TableCell<Container, Color> {    
	  private final ColorPicker colorPicker;

	    public ColorTableCell(TableColumn<Container, Color> column) {
	        this.colorPicker = new ColorPicker();
	        this.colorPicker.editableProperty().bind(column.editableProperty());
	        this.colorPicker.disableProperty().bind(column.editableProperty().not());
	        this.colorPicker.setOnShowing(event -> {
	            final TableView<Container> tableView = getTableView();
	            tableView.getSelectionModel().select(getTableRow().getIndex());
	            tableView.edit(tableView.getSelectionModel().getSelectedIndex(), column);       
	        });
	        this.colorPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
	        	if(newValue != null){
	        		System.out.println(newValue.getRed()+" "+newValue.getGreen()+" "+newValue.getBlue()+" ");
	        		Container selectedItem = getTableView().getSelectionModel().getSelectedItem();
	        		setColorForContainer(selectedItem, newValue);
	        		System.out.println(selectedItem);
	        	}
	        	
	            if(isEditing()) {
	                commitEdit(newValue);
	            }
	        });     
	        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
	    }

	    private void setColorForContainer(Container container, Color color){
	    	if(container != null)
	    		((model.Container) container).setColor(color);
	    }
	    
	    @Override
	    protected void updateItem(Color item, boolean empty) {
	        super.updateItem(item, empty);  

	        setText(null);  
	        if(empty) {     
	            setGraphic(null);
	        } else {        
	            this.colorPicker.setValue(item);
	            this.setGraphic(this.colorPicker);
	        } 
	    }
}