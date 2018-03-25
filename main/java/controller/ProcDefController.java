package controller;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.Operation;
import model.Process;
import model.StationType;
import service.OperationService;
import service.ProcessService;
import service.StationTypeService;

@Controller
public class ProcDefController {

	@FXML
	private ComboBox<StationType> stationTypeBox;
	@FXML
	private TextField operationNameField;
	@FXML
	private TextField batchSizeField;
	@FXML
	private TextArea operationDescriptionField;
	@FXML
	private Button addButton;
	@FXML
	private TextField procNameField;
	@FXML
	private TextField preTimeField;
	@FXML
	private TextField postTimeField;
	@FXML
	private Label artLabel;
	@FXML
	private Button removeButton;
	@FXML
	private TextArea procDescField;
	@FXML
	private TextField durationField;
	@FXML
	private TreeView<?> treePane;
	@FXML
	private Button saveButton;
	@FXML
	private Button applyButton;
	@FXML
	private ComboBox<Operation> nextOperationBox;

	@Autowired
	private StationTypeService typeService;
	@Autowired
	private ProcessService processService;
	@Autowired
	private OperationService operationService;
	@Autowired
	private ProcessController processController;
	@Autowired
	private OrdersController ordersController;

	private boolean treeIsRestarted = false;
	private SimpleBooleanProperty nameLabelProperty = new SimpleBooleanProperty(false),
			preFieldProperty = new SimpleBooleanProperty(false), postFieldProperty = new SimpleBooleanProperty(false),
			durationLabelProperty = new SimpleBooleanProperty(false),
			typeSelectionProperty = new SimpleBooleanProperty(false);

	// dane procesu
	private SimpleBooleanProperty processNameProperty = new SimpleBooleanProperty(false),
			batchSizeProperty = new SimpleBooleanProperty(false);
	private SimpleBooleanProperty activateApplyButton = new SimpleBooleanProperty();
	private SimpleBooleanProperty activateSaveButton = new SimpleBooleanProperty();

	private TreeItem root;
	private TreeItem longBelt, shortBelt, endOperation;

	private SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");

	@FXML
	void addEvent(ActionEvent event) {
		TreeItem<?> selectedItem = (TreeItem<?>) treePane.getSelectionModel().getSelectedItem();
		switch (selectedItem.getValue().toString()) {
		case "Nowy proces":
			;
			break;
		case "Pas dlugi":
			newItem(longBelt, "a");
			break;
		case "Pas krotki":
			newItem(shortBelt, "b");
			break;
		case "Operacja zakonczeniowa":
			newItem(endOperation, "a");
			break;
		default:
			newItem(selectedItem.getParent(), "c");

		}
	}

	private void newItem(TreeItem<?> operationTreeItem, String group) {
		Operation o = new Operation("Kolejna operacja");
		TreeItem treeItem;

		o.setGroup(group);
		o.setNextOperationObject(lastOperation());
		treeItem = new TreeItem(o);
		treeItem.setGraphic(singleOperationGraphicNotReady());
		if (operationTreeItem.getChildren().size() > 0)
			setNextOperationForNewOperationInTree(operationTreeItem, o);

		operationTreeItem.getChildren().add(treeItem);
	}

	private void setNextOperationForNewOperationInTree(TreeItem<?> operationTreeItem, Operation operation) {
		((Operation) operationTreeItem.getChildren().get(getLastIndexFromTreeItem(operationTreeItem)).getValue())
				.setNextOperationObject(operation);
	}

	private int getLastIndexFromTreeItem(TreeItem<?> operationTreeItem) {
		return operationTreeItem.getChildren().size() - 1;
	}

	private List<Operation> getCurrentChildList(TreeItem parent) {
		List<Operation> operationsList = new ArrayList<Operation>();

		if (parent.getValue().getClass().getTypeName().equals("model.Operation"))
			operationsList.addAll(parent.getParent().getChildren());
		else
			operationsList.addAll(parent.getChildren());

		return operationsList;
	}

	private Operation lastOperation() {
		ObservableList<TreeItem<Operation>> children = endOperation.getChildren();
		if (children.size() > 0)
			return children.get(0).getValue();

		return null;
	}

	@FXML
	void removeEvent(ActionEvent event) {
		TreeItem<Operation> selectedItem = getSelectedTreeItem();
		if (isTreeItemOperationType(selectedItem)) {
			int indexOf = selectedItem.getParent().getChildren().indexOf(selectedItem);
			if (indexOf > 0) {
				Operation previousOperation = selectedItem.getParent().getChildren().get(--indexOf).getValue();
				previousOperation.setNextOperationObject(selectedItem.getValue().getNextOperationObject());
			}
			selectedItem.getParent().getChildren().remove(selectedItem);
		}
		bindAllOperationsToOneBoolProperty();
	}

	@FXML
	void saveProcess(ActionEvent event) {
		try {
			Process saveProcess = processService.saveProcess(makeProcessForSave());
			operationService.save(getAllOperations(saveProcess));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		initTree();
		loadOperationsToTree(generateSampleOperations());
		processController.setProcessOnList();
		setTypeList();
	}

	private Process makeProcessForSave() {
		Process process = new Process();

		process.setName(procNameField.getText().trim());
		process.setDescription(procDescField.getText());
		process.setBatch(Short.parseShort(batchSizeField.getText().trim()));
		process.setArticle(ordersController.getArticleID());
		return process;
	}

	private List<Operation> getAllOperations(Process process) {
		List<Operation> operations = new ArrayList<Operation>();
		ObservableList<TreeItem> children = root.getChildren();
		for (TreeItem item : children) {
			ObservableList<TreeItem<Operation>> children2 = item.getChildren();
			for (TreeItem<Operation> item2 : children2) {
				Operation value = item2.getValue();
				value.setProces(process);
				operations.add(value);
			}
		}
		Collections.reverse(operations);
		return operations;
	}

	private void bindAllOperationsToOneBoolProperty() {
		activateSaveButton.unbind();

		List<Operation> operations = new ArrayList<Operation>();
		ObservableList<TreeItem> children = root.getChildren();
		for (TreeItem item : children) {
			ObservableList<TreeItem<Operation>> children2 = item.getChildren();
			for (TreeItem<Operation> item2 : children2) {
				Operation operation = item2.getValue();
				activateSaveButton.bind(operation.getReady().and(batchSizeProperty).and(processNameProperty));
			}
		}
	}

	@FXML
	void refreshTypes() {
		setTypeList();
	}

	@FXML
	void applyOperationChanges() {
		makeChanges();
	}

	private void makeChanges() {
		TreeItem<Operation> item = getSelectedTreeItem();
		Operation operation = item.getValue();
		applyChangesForOperation(operation);
		item.setValue(null);
		item.setValue(operation);
	}

	private void setValuesForSelectedItem() {
		TreeItem<Operation> item = getSelectedTreeItem();
		Operation op = item.getValue();
		item.setValue(null);
		item.setValue(op);
	}

	private void applyChangesForOperation(Operation o) {
		applyNameOperation(o);
		applyDurationOperation(o);
		applyPreTimeOperation(o);
		applyPostTimeOperation(o);
		applyStationTypeForOperation(o);
		applyDescriptionOperation(o);
	}

	private TreeItem<Operation> getSelectedTreeItem() {
		return (TreeItem<Operation>) treePane.getSelectionModel().getSelectedItem();
	}

	private void applyNameOperation(Operation operation) {
		operation.setName(operationNameField.getText().trim());
	}

	private void applyDurationOperation(Operation operation) {
		try {
			operation.setDuration(format.parse(durationField.getText().trim()));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	private void applyPreTimeOperation(Operation operation) {
		try {
			operation.setPreTime(format.parse(preTimeField.getText().trim()));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	private void applyPostTimeOperation(Operation operation) {
		try {
			operation.setPostTime(format.parse(postTimeField.getText().trim()));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	private void applyDescriptionOperation(Operation operation) {
		if (operationDescriptionField.getText() != null)
			operation.setDescription(operationDescriptionField.getText().trim());
	}

	private void applyStationTypeForOperation(Operation operation) {
		operation.setStationType(stationTypeBox.getSelectionModel().getSelectedItem());
	}

	private void initTree() {
		treeIsRestarted = true;
		clearFields();
		if (root != null)
			root.getChildren().clear();
		prepareTree();
		addTreeToPane();
		bindAllOperationsToOneBoolProperty();
	}

	private void addTreeToPane() {
		root.getChildren().addAll(longBelt, shortBelt, endOperation);
		treePane.setRoot((TreeItem) root);
	}

	private void prepareTree() {
		root = new TreeItem("Nowy proces");

		longBelt = new TreeItem("Pas dlugi");
		shortBelt = new TreeItem("Pas krotki");
		endOperation = new TreeItem("Operacja zakonczeniowa");

		setTreeItemsExpanded();
		setIconsForTree();
	}

	private void setTreeItemsExpanded() {
		root.setExpanded(true);
		longBelt.setExpanded(true);
		shortBelt.setExpanded(true);
		endOperation.setExpanded(true);
	}

	private void setIconsForTree() {
		root.setGraphic(iconForRoot());
		longBelt.setGraphic(iconForOperationBranch());
		shortBelt.setGraphic(iconForOperationBranch());
		endOperation.setGraphic(iconForOperationBranch());
	}

	private Node iconForRoot() {
		return new ImageView(new Image(getClass().getResourceAsStream("/icons/gearIco.png")));
	}

	private Node iconForOperationBranch() {
		return new ImageView(new Image(getClass().getResourceAsStream("/icons/operationIco.png")));
	}

	private Node singleOperationGraphicNotReady() {
		return new ImageView(new Image(getClass().getResourceAsStream("/icons/operationWrong.png")));
	}

	private Node singleOperationGraphicReady() {
		return new ImageView(new Image(getClass().getResourceAsStream("/icons/operationRight.png")));
	}

	private void clearFields() {
		operationNameField.clear();
		operationDescriptionField.clear();
		preTimeField.clear();
		postTimeField.clear();
		durationField.clear();
		nextOperationBox.getItems().clear();
		stationTypeBox.getItems().clear();
		procDescField.clear();
		procNameField.clear();
		batchSizeField.clear();
	}

	private void addingSampleOperationsToTree() {
		TreeItem[] items = { new TreeItem<Operation>(new Operation("Ciecie pasa dlugiego")),
				new TreeItem<Operation>(new Operation("Szycie pasa dlugiego")),
				new TreeItem<Operation>(new Operation("Ciecie pasa krotkiego")),
				new TreeItem<Operation>(new Operation("Szycie pasa krotkiego")),
				new TreeItem<Operation>(new Operation("Konfekcjonowanie")) };

		for (TreeItem t : items)
			t.setGraphic(singleOperationGraphicNotReady());

		longBelt.getChildren().addAll(items[0], items[1]);

		shortBelt.getChildren().addAll(items[2], items[3]);

		endOperation.getChildren().addAll(items[4]);
	}

	private List<Operation> generateSampleOperations() {
		Operation o11 = new Operation("Ciecie pasa dlugiego"), o12 = new Operation("Szycie pasa dlugiego"),
				o21 = new Operation("Ciecie pasa krotkiego"), o22 = new Operation("Szycie pasa krotkiego"),
				o3 = new Operation("Konfekcjonowanie");

		o11.setGroup("a");
		o21.setGroup("a");
		o22.setGroup("b");
		o21.setGroup("b");
		o3.setGroup("a");

		o11.setNextOperationObject(o12);
		o12.setNextOperationObject(o3);
		o21.setNextOperationObject(o22);
		o22.setNextOperationObject(o3);

		List<Operation> list = new ArrayList<Operation>();
		list.add(o11);
		list.add(o12);
		list.add(o21);
		list.add(o22);
		list.add(o3);

		return list;
	}

	public int groups = 0;

	public int getGroups() {
		return groups;
	}

	private List<Operation> findFirstOperations(List<Operation> list) {
		List<Operation> retList = new ArrayList<Operation>();
		char group = 'a';
		boolean isNextOperationTheSame = true;
		for (Operation operationFromList : list) {
			for (Operation subOperationFromList : list) {
				if (subOperationFromList.getNextOperationObject() == operationFromList)
					isNextOperationTheSame = false;
			}
			if (isNextOperationTheSame && operationFromList != null) {
				setGroups(operationFromList, group);
				retList.add(operationFromList);
				group += 1;
			}
			isNextOperationTheSame = true;
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

	private void loadOperationsToTree(List<Operation> operations) {
		List<Operation> firstOp = findFirstOperations(operations);
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
		bindAllOperationsToOneBoolProperty();
	}

	public void loadProcessFromBase(List<Operation> operations, Process process) {
		procNameField.setText(process.getName());
		procDescField.setText(process.getDescription());
		batchSizeField.setText(process.getBatch() + "");
		initTree();
		loadOperationsToTree(operations);
		setTypeList();
		bindAllOperationsToOneBoolProperty();
	}

	boolean test = false;// do tej metody poniżej

	private void addToTree(TreeItem treeItem, Operation o) {
		if (o == null)
			return;

		TreeItem item = new TreeItem(o);
		item.setGraphic(singleOperationGraphicNotReady());

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

		addToTree(treeItem, o.getNextOperationObject());
		bindAllOperationsToOneBoolProperty();
	}

	private void setOperatinoDataInLabels(TreeItem selectedItem) {

		if (!treeIsRestarted) {
			if (isTreeItemOperationType(selectedItem)) {
				setLabels((Operation) selectedItem.getValue());
			} else
				clearLabels();
		}
		treeIsRestarted = false;
	}

	private boolean isTreeItemOperationType(TreeItem item) {
		if (item != null)
			return item.getValue().getClass().getTypeName().equals("model.Operation");
		else
			return false;
	}

	private void clearLabels() {
		operationNameField.clear();
		operationDescriptionField.clear();
		preTimeField.clear();
		postTimeField.clear();
		durationField.clear();
		stationTypeBox.getSelectionModel().clearSelection();
	}

	private void setLabels(Operation o) {
		operationNameField.setText(o.getName());
		operationDescriptionField.setText(o.getDescription());
		preTimeField.setText(format.format(o.getPreTime()));
		postTimeField.setText(format.format(o.getPostTime()));
		durationField.setText(format.format(o.getDuration()));
		stationTypeBox.getSelectionModel().select(o.getStationType());
		nextOperationBox.getItems().clear();
		nextOperationBox.getItems().add(o.getNextOperationObject());
		nextOperationBox.getSelectionModel().selectFirst();

	}

	@FXML
	private void initialize() {
		initNewProcess();
	}

	private void initNewProcess() {
		applyButton.setVisible(false);
		initTree();
		loadOperationsToTree(generateSampleOperations());
		setBindings();
		setTypeList();
		treeIsRestarted = false;
	}

	public void setTypeList() {
		stationTypeBox.getItems().clear();
		stationTypeBox.getItems().addAll(typeService.getStationTypes());
	}

	private void setBindings() {

		treePane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem>() {

			@Override
			public void changed(ObservableValue<? extends TreeItem> arg0, TreeItem arg1, TreeItem arg2) {
				setOperatinoDataInLabels(arg0.getValue());
			}
		});

		preTimeField.textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
				if (isMatchToTimeFormat(arg0.getValue())) {
					preTimeField.setStyle("-fx-border-color: green");
					preFieldProperty.set(true);
					applyPreTimeOperation(getSelectedTreeItem().getValue());
				} else {
					preTimeField.setStyle("-fx-border-color: red");
					preFieldProperty.set(false);
				}

			}
		});

		postTimeField.textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
				if (isMatchToTimeFormat(arg0.getValue())) {
					postTimeField.setStyle("-fx-border-color: green");
					postFieldProperty.set(true);
					applyPostTimeOperation(getSelectedTreeItem().getValue());
				} else {
					postTimeField.setStyle("-fx-border-color: red");
					postFieldProperty.set(false);
				}

			}
		});

		durationField.textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
				if (isMatchToTimeFormat(arg0.getValue())) {
					durationField.setStyle("-fx-border-color: green");
					durationLabelProperty.set(true);
					applyDurationOperation(getSelectedTreeItem().getValue());
				} else {
					durationField.setStyle("-fx-border-color: red");
					durationLabelProperty.set(false);
				}

			}
		});

		operationNameField.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
				if (!arg0.getValue().trim().isEmpty()) {
					operationNameField.setStyle("-fx-border-color: green");
					nameLabelProperty.set(true);
					applyNameOperation(getSelectedTreeItem().getValue());
					setValuesForSelectedItem();
				} else {
					operationNameField.setStyle("-fx-border-color: red");
					nameLabelProperty.set(false);
				}
			}
		});

		procNameField.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
				if (!arg0.getValue().trim().isEmpty()) {
					procNameField.setStyle("-fx-border-color: green");
					processNameProperty.set(true);
				} else {
					procNameField.setStyle("-fx-border-color: red");
					processNameProperty.set(false);
				}
			}
		});

		batchSizeField.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
				if (arg0.getValue().trim().matches("\\d+")) {
					batchSizeField.setStyle("-fx-border-color: green");
					batchSizeProperty.set(true);
				} else {
					batchSizeField.setStyle("-fx-border-color: red");
					batchSizeProperty.set(false);
				}
			}
		});

		stationTypeBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<StationType>() {

			@Override
			public void changed(ObservableValue<? extends StationType> arg0, StationType arg1, StationType arg2) {
				if (arg0.getValue() != null) {
					stationTypeBox.setStyle("-fx-border-color: green");
					typeSelectionProperty.set(true);
					applyStationTypeForOperation(getSelectedTreeItem().getValue());

				} else {
					stationTypeBox.setStyle("-fx-border-color: red");
					typeSelectionProperty.set(false);
				}

			}
		});

		activateApplyButton.bind(nameLabelProperty.and(preFieldProperty).and(postFieldProperty)
				.and(durationLabelProperty).and(typeSelectionProperty));

		activateApplyButton.addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
				setOperationReadyAndSetIcon(arg0.getValue());
			}
		});
		 bindAllOperationsToOneBoolProperty();

		activateSaveButton.bind(batchSizeProperty.and(processNameProperty));
		
		activateSaveButton.addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
				if (arg0.getValue())
					saveButton.setDisable(false);
				else
					saveButton.setDisable(true);

			}

		});
	}

	private boolean isMatchToTimeFormat(String testValue) {
		return testValue.matches("\\d{2}:\\d{2}:\\d{2}");
	}

	private void setOperationReadyAndSetIcon(boolean ifReady) {
		TreeItem<Operation> selectedTreeItem = getSelectedTreeItem();
		if (ifReady) {
			selectedTreeItem.setGraphic(singleOperationGraphicReady());
			selectedTreeItem.getValue().setReady(true);
		} else {
			selectedTreeItem.setGraphic(singleOperationGraphicNotReady());
			selectedTreeItem.getValue().setReady(false);
		}
	}

	@FXML
	private void newOperation() {
		treePane.getSelectionModel().clearSelection();
		clearFields();
		clearLabels();
		initNewProcess();
	}

}
