package controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import controller.functions.ShortTableCell;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseEvent;
import model.Batch;
import model.Operation;
import model.Orders;
import model.Process;
import printStuff.PrintArtBarCode;
import service.BatchService;
import service.OperationService;
import service.ProcessService;

@Controller
public class ProcessController {
	@FXML
	private TableView<Batch> partTable;
	@FXML
	private TableColumn<Batch, String> lpColumn;
	@FXML
	private TableColumn<Batch, String> groupColumn;
	@FXML
	private TableColumn<Batch, String> idColumn;
	@FXML
	private TableColumn<Batch, Short> amountColumn;
	@FXML
	private TableColumn<Batch, String> descriptionColumn;
	@FXML
	private MenuItem printMenu;
	@FXML
	private MenuItem selectAllMenu;
	@FXML
	private MenuItem undoTableMenuItem;
	@FXML
	private MenuItem joinDownMenuItem;
	@FXML
	private ListView<Process> procList;
	@FXML
	private ContextMenu contextMenu;
	@FXML
	private MenuItem procMenuItem;
	@FXML
	private MenuItem editMenuItem;
	@FXML
	private MenuItem deleteMenuItem;
	@FXML
	private TextArea procDescArea;
	
	@Autowired
	private OrdersController ordersController;
	@Autowired
	private ProcessService procService;
	@Autowired
	private OperationService operationService;
	@Autowired
	private BatchService batchService;
	@Autowired
	private OperationsController operationsController;
	@Autowired
	private BatchSettingsController batchSettingsController;
	
	private List<Batch> batches = new ArrayList<Batch>();
	private int batchSize;
	private int groups = -1;//liczba grup z procesu - do tworznia partii
	private final int ORDER_IN_PROGRESS = 1;
	private boolean loadMode = false;
	
	
	public Map<String, List<Batch>> getBatchesWrapedInMap(){
		Map <String, List<Batch>> map = new TreeMap<String, List<Batch>>();
		map.put("a", new ArrayList<Batch>());
		map.put("b", new ArrayList<Batch>());
		map.put("c", new ArrayList<Batch>());
		map.put("d", new ArrayList<Batch>());
		for(Batch b : batches)
			if(map.containsKey(b.getGrupa()))
				map.get(b.getGrupa()).add(b);
			
		return map;
	}
	
	public List<Batch> getListOfAllBatches(){
		return this.batches;
	}
	
	public void clearTable(){
		partTable.getItems().clear();
	}
	
	public void setProcessOnList(){
		Orders selectedOrder = ordersController.getSelectedOrder();
		int selectedArt = ordersController.getArticleID();
		List<Process> list;
		
		boolean isInProgress = selectedOrder.isInProgress();
		
		undoTableMenuItem.setDisable(isInProgress);
		joinDownMenuItem.setDisable(isInProgress);
		
		procList.getItems().clear();
		if(isOrderInProgress()){
			int orderId = selectedOrder.getOrderId();
			list = selectedOrder.getArticle().getProcessList();
			this.batches = batchService.getByOrder(orderId);
			procList.getSelectionModel().select(0);
			loadMode = true;
		}else{
			list = procService.getByArticle(selectedArt);
			loadMode = false;
		}
		procList.getItems().addAll(list);
	}
	
	private boolean isOrderInProgress(){
		return (ordersController.getSelectedOrder().getOrderInProgress() == ORDER_IN_PROGRESS)?true:false;
	}
	
	@FXML
	private void initialize() {
		onProcessSelection();
		tableSettings();
	}

	private void onProcessSelection(){
		procList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Process>() {

			@Override
			public void changed(ObservableValue<? extends Process> arg0, Process arg1, Process arg2) {
				Process proc = arg0.getValue();
				
				if (proc != null) {
					setProcessParamsOnWindow(arg0.getValue());
					batchSize = arg0.getValue().getBatch();
					List<Operation> operationForProcess = operationService.getOperationsForProcess(proc);
					Collections.reverse(operationForProcess);
					operationsController.loadProcessFromBase(operationForProcess, proc);
					groups = operationsController.groups;
					if(!loadMode)
						generateBatches(ordersController.getOrderAmount(), batchSize);
					else{
						partTable.getItems().clear();
						partTable.getItems().addAll(batches);
					}
				}
			}
		});
	}
	
	private void tableSettings() {
		partTable.isEditable();

		idColumn.setCellValueFactory(new PropertyValueFactory<Batch, String>("Zlecenie"));
		idColumn.setVisible(true);

		amountColumn.setCellValueFactory(new PropertyValueFactory<Batch, Short>("Ilosc"));
		amountColumn.setEditable(true);

		groupColumn.setCellValueFactory(new PropertyValueFactory<Batch, String>("Grupa"));//

		lpColumn.setCellValueFactory(new PropertyValueFactory<Batch, String>("NrPartii"));

		partTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		partTable.getSelectionModel().cellSelectionEnabledProperty();
		partTable.setEditable(true);

		amountColumn.setEditable(true);
		amountColumn.setCellValueFactory(new PropertyValueFactory<Batch, Short>("Ilosc"));
		amountColumn.setCellFactory(cal -> new ShortTableCell());
		amountColumn.setOnEditCommit(new EventHandler<CellEditEvent<Batch, Short>>() {
			@Override
			public void handle(CellEditEvent<Batch, Short> t) {
				short newValue = t.getNewValue();
				System.out.println(newValue);
				int index = partTable.getSelectionModel().getSelectedIndex();
				if(index == 0){
					generateBatches(ordersController.getOrderAmount(), newValue);
				}
				else{
					String grupa = partTable.getSelectionModel().getSelectedItem().getGrupa();
					if(!partTable.getItems().get(--index).getGrupa().equals(grupa)){
						generateBatchesForNextGroup(ordersController.getOrderAmount(), newValue, grupa.charAt(0));
					}		
				}
				
				if(newValue == 0){
					Batch b = partTable.getSelectionModel().getSelectedItem();
					batches.remove(b);
					partTable.getItems().clear();
					partTable.getItems().addAll(batches);
				}
			}
		});

		descriptionColumn.setCellFactory(TextFieldTableCell.forTableColumn());
		descriptionColumn.setEditable(true);
		descriptionColumn.setOnEditCommit(new EventHandler<CellEditEvent<Batch, String>>() {
			@Override
			public void handle(CellEditEvent<Batch, String> t) {
				((Batch) t.getTableView().getItems().get(t.getTablePosition().getRow())).setOpis(t.getNewValue());
			}
		});
	}
	
	private void generateBatchesForNextGroup(int amount, int batchSize, char group) {

		int normalBatches = (int) amount / batchSize;
		int lastBatch = (int) amount % batchSize;
		deleteGroupFromTable(group+"");
		int batchNum = getLastIndexFromTable()+1;
		for (int i = 0; i < normalBatches; i++) {
			makeBatch(batchSize, group, batchNum);
			batchNum += 1;
		}
		if (lastBatch > 0) {
			makeBatch(lastBatch, group, batchNum);
			batchNum += 1;
		}
		partTable.getItems().clear();
		partTable.getItems().addAll(batches);

	}
	
	private void deleteGroupFromTable(String group){
		List<Batch> toRemove = new ArrayList<Batch>();
		for(Batch b : batches)
			if(b.getGrupa().equals(group))
				toRemove.add(b);
			
		batches.removeAll(toRemove);
	}
	
	private int getLastIndexFromTable(){
		int size = partTable.getItems().size() - 1;
		return size;
	}
	
	private void generateBatches(int amount, int batchSize){
		int normalBatches = (int)amount/batchSize;
		int lastBatch  = (int)amount%batchSize;
		int batchNum = 0;
		char group = 'a';
			
		batches.clear();
		for(int j = 0; j < groups; j ++){
			for(int i = 0; i < normalBatches; i++){
				makeBatch(batchSize, group, batchNum);
				batchNum += 1;
			}
			if(lastBatch > 0){
				makeBatch(lastBatch, group, batchNum);
				batchNum += 1;
			}
			group += 1;
		}
		partTable.getItems().clear();
		partTable.getItems().addAll(batches);

	}
	
	private void makeBatch(int amount, char group, int batchNum){
		Batch b = new Batch();
		b.setIlosc((short) amount);
		b.setZlecenie(ordersController.getOrderID());
		b.setGrupa(group+"");
		b.setNrPartii(batchNum);
		batches.add(b);
	}
	
	private void setProcessParamsOnWindow(Process proc){
		if(procList.getSelectionModel().getSelectedItem() != null){
			String info = proc.getName()+"\n ------------------------- \n partia: "+proc.getBatch()+""
					+ "\n \n"+proc.getDescription()+"\n Liczba sztuk do wykonania: "+ordersController.getSelectedOrder().getAmount();
			procDescArea.setText(info);
		}
	}

	@FXML
	public void print(ActionEvent event) {
		ObservableList<Batch> items = partTable.getSelectionModel().getSelectedItems();
		String [][] listToPrint = new String[items.size()][4];
		for(int i = 0; i < items.size(); i++){
			Batch selected = items.get(i);
			listToPrint[i][0] = ordersController.getSelectedOrder().getArticle().getName();
			listToPrint[i][1] = selected.getNrPartii()+"("+selected.getIlosc()+")";
			listToPrint[i][2] = " ";
			listToPrint[i][3] = selected.getIDPartia()+"-"+selected.getNrPartii();
		}
		new PrintArtBarCode(0, 0, listToPrint);
	}

	@FXML
	public void selectAll(ActionEvent event) {
		partTable.getSelectionModel().selectAll();
	}

	@FXML
	public void undoPortions(ActionEvent event) {
		clearSelectedBatchesInBatchSettings();
		generateBatches(ordersController.getOrderAmount(), batchSize);
	}

	@FXML
	public void joinDown(ActionEvent event) {
		joinDown();
	}
	private  void joinDown(){
		clearSelectedBatchesInBatchSettings();
		Batch selectedItem = partTable.getSelectionModel().getSelectedItem();
		int indexOf = partTable.getItems().indexOf(selectedItem);
		String grupa = selectedItem.getGrupa();
		
		indexOf++;
		Batch actualItem = selectedItem;
		
		while (actualItem.getGrupa().equals(grupa) && indexOf <= partTable.getItems().size() - 1) {
			actualItem = partTable.getItems().get(indexOf);
			if (actualItem.getGrupa().equals(grupa)) {
				selectedItem.setIlosc((short) (actualItem.getIlosc() + selectedItem.getIlosc()));
				batches.remove(actualItem);
			}
			indexOf++;
		}
		
		partTable.getItems().clear();
		partTable.getItems().addAll(batches);
		
	}
	
	@FXML
	public void printLabel(){
		System.out.println("label"+ partTable.getItems().get(0));
		printBatchLabel();
	}
	
	private void printBatchLabel(){
		ObservableList<Batch> items = partTable.getSelectionModel().getSelectedItems();
		String [][] listToPrint = new String[items.size()][4];
		Orders selectedOrder = ordersController.getSelectedOrder();
		if(!items.isEmpty())
			for(int i = 0; i < items.size(); i++){
				listToPrint = new String[items.size()][4];
				listToPrint[i][0] = selectedOrder.getArticle().getName();
				listToPrint[i][1] = selectedOrder.getNumber()+"("+selectedOrder.getAmount()+")";
				listToPrint[i][2] = " ";
				listToPrint[i][3] = selectedOrder.getOrderId()+"-"+selectedOrder.getNumber();
			}else{
				listToPrint = new String[1][4];
				listToPrint[0][0] = selectedOrder.getArticle().getName();
				listToPrint[0][1] = selectedOrder.getNumber()+"("+selectedOrder.getAmount()+")";
				listToPrint[0][2] = " ";
				listToPrint[0][3] = selectedOrder.getOrderId()+"-"+selectedOrder.getNumber();
				
			}
			
		new PrintArtBarCode(0, 0, listToPrint);
	}
	
	private void clearSelectedBatchesInBatchSettings(){
		batchSettingsController.resetLists();
	}
	
	@FXML
	public void listEvent(MouseEvent event) {
	}
	@FXML
	public void addProces(ActionEvent event) {
	}
	@FXML
	public void editEvent(ActionEvent event) {
	}

	@FXML
	public void deleteEvent(ActionEvent event) {
		
	}
}
