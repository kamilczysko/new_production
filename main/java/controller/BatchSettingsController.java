package controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.Batch;
import model.Operation;
import model.Station;
import model.StationType;
import service.StationService;

@Controller
public class BatchSettingsController {
	@FXML
	private ListView<Batch> batchList;
	@FXML
	private Button addButton;
	@FXML
	private Button removeButton;
	@FXML
	private ComboBox<Station> stationBox;
	@FXML
	private ListView<Batch> stationList;
	
	@Autowired
	private StationService stationService;
	
	private Operation operation;
	private MultiValueMap<Long, Batch> mapWithBatchesAddedToStations;
	private TreeItem actualItem;
	private List<Batch> wholeBatchList;
	
	public List<Batch> getWholeBatchList(){
		return this.wholeBatchList;
	}
	
	@FXML 
	private void initialize(){
		addButton.setDisable(true);
		removeButton.setDisable(true);
		batchList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		stationList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		
		stationList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Batch>() {

			@Override
			public void changed(ObservableValue<? extends Batch> arg0, Batch arg1, Batch arg2) {
								
				if(stationList.getSelectionModel().getSelectedItems().size() > 0)
					removeButton.setDisable(false);
				else
					removeButton.setDisable(true);
				
			}
		});
		
		batchList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Batch>() {

			@Override
			public void changed(ObservableValue<? extends Batch> arg0, Batch arg1, Batch arg2) {
				if(batchList.getSelectionModel().getSelectedItems().size() > 0)
					addButton.setDisable(false);
				else
					addButton.setDisable(true);
				
			}
		});	
	}
	
	private Node setSingleOperationGraphic(boolean done){
		if(!done)
			return new ImageView(new Image(getClass().getResourceAsStream("/icons/operationWrong.png")));
		return new ImageView(new Image(getClass().getResourceAsStream("/icons/operationRight.png")));
	}
	
	public void resetLists(){
		batchList.getItems().clear();
		stationList.getItems().clear();
	}
		
	public void setBatchList(List<Batch> list){
		
		batchList.getItems().clear();
		wholeBatchList = new ArrayList<Batch>();
		for(Batch b : list)
			if(!hasElement(b)){
				wholeBatchList.add(b);
			}
		batchList.getItems().addAll(wholeBatchList);
		stationBox.getSelectionModel().selectFirst();
	}
	
	private boolean hasElement(Batch b){
		Set<Long> key = mapWithBatchesAddedToStations.keySet();
		for(Long k : key)
			if(mapWithBatchesAddedToStations.get(k).contains(b))
				return true;
		
		return false;
	}
	
	private void initStationList(StationType st){
		List<Station> byType = stationService.getByType(st.getIDTypu());
		stationBox.getItems().clear();
		stationBox.getItems().addAll(byType);
	}
	
	public void setOperationData(Operation operation){
		this.operation = operation;
		initStationList(operation.getStationType());
		this.mapWithBatchesAddedToStations = operation.getMap();
	}
	
	private Station selectedStation;
	
	@FXML
	private void selectStation(){
		this.selectedStation = stationBox.getSelectionModel().getSelectedItem();
		if(selectedStation != null)
			setStatList();			
	}
	
	private void setStatList(){
		stationList.getItems().clear();
		List<Batch> objects = mapWithBatchesAddedToStations.get(selectedStation.getStationId());	
		
		
		if(objects != null){
			
			objects.sort(Comparator.comparing(Batch::getNrPartii));
			
			List<Batch> list = objects;
			for(Batch b : list){
				stationList.getItems().add(b);
			}
		}
	}
	
	@FXML
	public void selectAllInBatchList(ActionEvent event) {
		batchList.getSelectionModel().selectAll();
	}
	@FXML
	public void addBatches(ActionEvent event) {
		ObservableList<Batch> selectedItems = batchList.getSelectionModel().getSelectedItems();
		long key = selectedStation.getStationId();
		for(Batch b : selectedItems)
			mapWithBatchesAddedToStations.add(key, b);
		
		mapWithBatchesAddedToStations.get(key);
	
		
		this.operation.setMap(mapWithBatchesAddedToStations);
		setStatList();
		batchList.getItems().removeAll(selectedItems);
		
		if(batchList.getItems().isEmpty()){
			this.actualItem.setGraphic(setSingleOperationGraphic(true));
			this.operation.setReady(true);
		}
	}
	
	// Event Listener on Button[#removeButton].onAction
	@FXML
	public void removeBatches(ActionEvent event) {
		List<Batch> selectedItems = stationList.getSelectionModel().getSelectedItems();
		long key = selectedStation.getStationId();
		
		for(Batch b : selectedItems)
			mapWithBatchesAddedToStations.get(key).remove(b);
		
		batchList.getItems().addAll(selectedItems);
		stationList.getItems().removeAll(selectedItems);
		this.operation.setMap(mapWithBatchesAddedToStations);

		this.operation.setReady(false);
		this.actualItem.setGraphic(setSingleOperationGraphic(false));
		
	}
	
	// Event Listener on MenuItem.onAction
	@FXML
	public void selectAllInStationList(ActionEvent event) {
		stationList.getSelectionModel().selectAll();
	}
	
	public boolean isReady(){
		
		return batchList.getItems().isEmpty();
	}

	public void setItem(TreeItem item) {
		
		this.actualItem = item;
	}
}
