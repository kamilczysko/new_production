package controller;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import controller.functions.SaveProduction;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import model.Operation;
import model.Process;
import model.Station;
import model.StationType;
import model.Timing;
import service.StationService;
import service.TimingService;
import tornadofx.control.DateTimePicker;

@Controller
public class OperationsController {

	@FXML
	private TreeView treeView;
	@FXML
	private TextArea operationDescriptionField;
	@FXML
	private ListView<Station> stationListBusy;
	@FXML
	private TextField preTimeField;
	@FXML
	private TextField postTimeField;
	@FXML
	private Button updateButton;
	@FXML
	private Button saveButton;
	@FXML
	private Button printButton;
	@FXML
	private VBox box;
	@FXML
	private ProgressIndicator progressIndicator;

	private DateTimePicker timePicker;

	@Autowired
	private BatchSettingsController batchSetController;
	@Autowired
	private ProcessController processController;
	@Autowired
	private ChartController chartController;
	@Autowired
	private StationService stationService;
	@Autowired 
	TimingService timingService;
	@Autowired
	private SaveProduction saveProd;

	private TreeItem root;
	private TreeItem longBelt, shortBelt, endOperation;
	
	private	final Node rootIcon =  new ImageView(new Image(getClass().getResourceAsStream("/icons/gearIco.png")));
	private SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
	private Operation selectedOperation;
	private List<Operation> firstOp;
	private SimpleBooleanProperty readyToSave = new SimpleBooleanProperty(false);
	
	public void setProgress(double progress){
		progressIndicator.setProgress(progress);
	}

	@FXML
	private void initialize() {		
		saveButton.setDisable(true);
		setTimePicker();
		setListenersToGUIitems();
		initTree();
	}
	private void setTimePicker(){
		
		timePicker = new DateTimePicker();
		timePicker.prefWidth(250);
		timePicker.prefHeight(150);
		box.getChildren().add(timePicker);
		setPresentMoment();
	}
	
	private void setListenersToGUIitems(){
		
		treeView.getSelectionModel().selectedItemProperty().addListener((ChangeListener<TreeItem>) (arg0, arg1, arg2) -> {
			if (arg0.getValue() != null)
				batchSetController.resetLists();
			try {
				selectedOperation(arg0.getValue());
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});

		preTimeField.textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
				if (arg0.getValue().matches("\\d{2}:\\d{2}:\\d{2}")) {
					if (selectedOperation != null) {
						try {
							selectedOperation.setPreTime(format.parse(arg0.getValue()));
						} catch (ParseException e) {
							e.printStackTrace();
						}
						preTimeField.setStyle("-fx-border-color: green");
					}
				} else
					preTimeField.setStyle("-fx-border-color: red");

			}
		});

		postTimeField.textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
				if (arg0.getValue().matches("\\d{2}:\\d{2}:\\d{2}")) {
					if (selectedOperation != null) {
						try {
							selectedOperation.setPostTime(format.parse(arg0.getValue()));
						} catch (ParseException e) {
							e.printStackTrace();
						}
						postTimeField.setStyle("-fx-border-color: green");
					}
				} else
					postTimeField.setStyle("-fx-border-color: red");
			}
		});
	}
	
	public void clearPane(){
		initTree();
	}
	
	private boolean isTreeItemOperationType(TreeItem item){
		return item.getValue().getClass().getTypeName().equals("model.Operation");
	}
	
	private void selectedOperation(TreeItem item) throws SQLException {
		
		if (item != null) {
			if (isTreeItemOperationType(item)) {
				this.selectedOperation = ((Operation) item.getValue());
				operationDescriptionField.setText(this.selectedOperation.getDescription());
				preTimeField.setText(format.format(this.selectedOperation.getPreTime()));
				postTimeField.setText(format.format(this.selectedOperation.getPostTime()));
				batchSetController.setItem(item);
				batchSetController.setOperationData(this.selectedOperation);

				switch (item.getParent().getValue().toString()) {
				case "Pas dlugi":
					batchSetController.setBatchList(processController.getBatchesWrapedInMap().get("a"));
					break;
				case "Pas krotki":
					batchSetController.setBatchList(processController.getBatchesWrapedInMap().get("b"));
					break;
				case "Operacja zakonczeniowa":
					batchSetController.setBatchList(processController.getBatchesWrapedInMap().get("a"));
					break;
				default:
					;
				}
			}
			fillBusyListWithStations();
		}
	}
	
	private void fillBusyListWithStations() throws SQLException{
		stationListBusy.getItems().clear();
		List<Station> listForPossibleDates = new ArrayList<Station>();
		StationType stationType = this.selectedOperation.getStationType();
		List<Station> byType = stationService.getByType(stationType.getIDTypu());
		for(Station s : byType){
			Date findFreeStations = findFreeStations(s);
			s.setPossibleDate(findFreeStations);
			listForPossibleDates.add(s);
		}
		stationListBusy.getItems().addAll(listForPossibleDates);
	}

	private Date findFreeStations(Station station) throws SQLException{
		List<Timing> timings = timingService.getByStartDateAndStation(station, getDateFromDatePicker());
		Timing timing = null;
		if(timings == null || timings.isEmpty())
			return null;
		else
			timing = timings.get(timings.size()-1);
		
		if(timing.getEnd() == null || timing.getEnd().getYear() == 1900){
			long time = timing.getStart().getTime() + timing.getDurationOfWholeProductionInSeconds();
			return new Date(time);
		}else
			return timing.getEnd();
	}

	private void initTree() {

		if (root != null)
			root.getChildren().clear();

		root = new TreeItem("Nowy proces");
		root.setExpanded(true);

		longBelt = new TreeItem("Pas dlugi");
		shortBelt = new TreeItem("Pas krotki");
		endOperation = new TreeItem("Operacja zakonczeniowa");

		longBelt.setExpanded(true);
		shortBelt.setExpanded(true);
		endOperation.setExpanded(true);

		root.setGraphic(rootIcon);
		shortBelt.setGraphic(setGraphicForOperationBranch());
		longBelt.setGraphic(setGraphicForOperationBranch());
		endOperation.setGraphic(setGraphicForOperationBranch());

		root.getChildren().addAll(longBelt, shortBelt, endOperation);


		treeView.setRoot((TreeItem) root);		
	}

	private Node setGraphicForOperationBranch(){
		return new ImageView(new Image(getClass().getResourceAsStream("/icons/operationIco.png")));
	}
	
	private Node setSingleOperationGraphic(){
			return new ImageView(new Image(getClass().getResourceAsStream("/icons/operationWrong.png")));
	}
	
	public void loadProcessFromBase(List<Operation> operations, Process process) {
		initTree();
		loadOperationsToTree(operations);
	}

	
	private void loadOperationsToTree(List<Operation> operations) {
		setBoolProp(operations);
		firstOp = findFirstOperations(operations);
		for (Operation o : firstOp) {
			switch (o.getGroup()) {
			case "a":
				addToTree(longBelt, o);
				break;
			case "b":
				addToTree(shortBelt, o);
				break;
			default:

			}
		}
	}
	
	private void setBoolProp(List<Operation> list){
		for(Operation o : list)
			readyToSave.bind(o.getReady());
		
		readyToSave.addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
				saveButton.setDisable(false);
			}
		});
	}

	boolean test = false;// do tej metody poniżej

	private void addToTree(TreeItem treeItem, Operation o) {
		if (o == null)
			return;

		TreeItem item = new TreeItem(o);

		if (o.getNextOperationObject() != null)
			treeItem.getChildren().add(item);
		else {
			test = false;
			ObservableList<TreeItem> children = endOperation.getChildren();
			children.forEach(child -> {
				if (child.getValue() == o) {
					test = true;
				}
			});
			if (!test)
				endOperation.getChildren().add(item);
		}
		
		item.setGraphic(setSingleOperationGraphic());

		addToTree(treeItem, o.getNextOperationObject());
	}

	int groups = 0;

	private List<Operation> findFirstOperations(List<Operation> list) {
		List<Operation> retList = new ArrayList<Operation>();
		char group = 'a';
		boolean test = true;
		for (Operation a : list) {
			for (Operation b : list) {
				if (b.isNextOperation(a))
					test = false;
			}
			if (test && a != null) {
				setGroups(a, group);
				retList.add(a);
				group += 1;
			}
			test = true;
		}
		groups = retList.size();
		return retList;
	}

	void setGroups(Operation o, char group) {
		if (o == null)
			return;
		o.setGroup(group + "");
		setGroups(o.getNextOperationObject(), group);
	}

	@FXML
	public void updateOperation(ActionEvent event) {
		// TODO Autogenerated
	}

	@FXML
	public void save(ActionEvent event) {
		setWaitCursor();
		Date d = getDateFromDatePicker();
		try {
			saveProd.initStationsMap();
			saveProd.saveBatches(processController.getListOfAllBatches());
			saveProd.saveProduction(firstOp, d);
		} catch (SQLException e) {
			alertWindow();
			e.printStackTrace();
		}
		chartController.setOrdersList();
		doneWindow();
	}
	
	private void setWaitCursor(){
		updateButton.getScene().setCursor(Cursor.WAIT);
	}
	
	private void alertWindow(){
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Blad");
		alert.setHeaderText("Nie mozna zapisac elementu do bazy.");
		alert.setContentText("Wystapil blad w zapisywaniu.");

		alert.showAndWait();
	}
	
	private void doneWindow(){
		setDefaultCursor();
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Zapisano");
		alert.setHeaderText("");
		alert.setContentText("Produkcja zapisana w bazie.");

		alert.showAndWait();
	}
	
	private void setDefaultCursor(){
		updateButton.getScene().setCursor(Cursor.DEFAULT);
	}

	private Date getDateFromDatePicker(){
		ZoneId defaultZoneId = ZoneId.systemDefault();
		LocalDateTime dateTimeValue = timePicker.getDateTimeValue();
		Date d = Date.from(dateTimeValue.atZone(defaultZoneId).toInstant());
		return d;
	}
	
	@FXML
	public void printAll(ActionEvent event) {
		// TODO Autogenerated
	}

	@FXML
	public void setPresentMoment() {
		timePicker.setDateTimeValue(LocalDateTime.now());
	}

}
