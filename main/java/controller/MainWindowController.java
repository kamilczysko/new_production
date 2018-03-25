package controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import controller.window.StationCreate;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import service.StationService;
import service.StationTypeService;

@Controller
public class MainWindowController {
	@FXML
	private Tab orderTab;
	@FXML
	private Tab processTab;
	@FXML
	private Tab operationsTab;

	@Autowired
	private OrdersController ordersController;
	
	@Autowired
	private StationCreate stationCreateWindow;// = new StationCreate();

	@Autowired
	private StationService stationService;
	
	@Autowired
	private StationTypeService stationTypeService;

	@FXML
	private void initialize() {
		stationCreateWindow.getController().setServices(this.stationService, this.stationTypeService);		
	}
		
	public void mainInit(){
		stationCreateWindow.getController().setServices(this.stationService, this.stationTypeService);
	}

	// Event Listener on MenuItem.onAction
	@FXML
	public void refreshEverything(ActionEvent event) {
		ordersController.setOrdersListInComboBox();
	}

	// Event Listener on MenuItem.onAction
	@FXML
	public void EndMenuItemEvent(ActionEvent event) {
		Platform.exit();
	}

	// Event Listener on MenuItem.onAction
	@FXML
	public void addStationView(ActionEvent event) {
		stationCreateWindow.show();
	}
	
	public void lockProcessTab(boolean lock){
		operationsTab.setDisable(lock);
	}

}
