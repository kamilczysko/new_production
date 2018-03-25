package controller;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.controlsfx.control.CheckListView;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import model.Station;
import model.StationType;
import service.StationService;
import service.StationTypeService;

public class StationDefinitionController {
	@FXML
	private TextField stationTypeName;
	@FXML
	private TextArea stationTypeDescription;
	@FXML
	private Button saveTypeButton;
	@FXML
	private ListView<StationType> stationTypeList;
	@FXML
	private TextField stationNameField;
	@FXML
	private TextArea stationDescriptionArea;
	@FXML
	private TextField preTimeField;
	@FXML
	private TextField postTimeField;
	@FXML
	private Button saveStationButton;
	@FXML
	private Button editTypeButton;
	@FXML
	private Button deleteTypeButton;
	@FXML
	private Button editStationButton;
	@FXML
	private Button deleteStationButton;
	@FXML
	private ListView<Station> stationList;
	@FXML
	private HBox container;

	private CheckListView<StationType> typeCheckList;

	private StationService stationService;
	private StationTypeService stationTypeService;

	private ObservableList<Station> stations;
	private ObservableList<StationType> types;

	private SimpleBooleanProperty canSaveStation = new SimpleBooleanProperty();
	private SimpleBooleanProperty preTimeSave = new SimpleBooleanProperty(false);
	private SimpleBooleanProperty postTimeSave = new SimpleBooleanProperty(false);
	private SimpleBooleanProperty stationNameSave = new SimpleBooleanProperty(false);
	private SimpleBooleanProperty typeNameSave = new SimpleBooleanProperty(false);
	private SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");

	private static Map<Long, Station> mapOfAllStations;

	public void setServices(StationService stationService, StationTypeService stationTypeService) {
		this.stationService = stationService;
		this.stationTypeService = stationTypeService;

		setStationLists();
		setTypeList();
	}

	@FXML
	private void initialize() {

		typeCheckList = new CheckListView<>();
		typeCheckList.setPrefSize(200, 300);
		container.getChildren().add(typeCheckList);

		setBindings();
	}

	private void setBindings() {

		postTimeField.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
				if (arg0.getValue().matches("\\d{2}:\\d{2}:\\d{2}")) {
					postTimeField.setStyle("-fx-border-color: green");
					postTimeSave.setValue(true);
				} else {
					postTimeField.setStyle("-fx-border-color: red");
					postTimeSave.setValue(false);
				}

			}
		});

		preTimeField.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
				if (arg0.getValue().matches("\\d{2}:\\d{2}:\\d{2}")) {
					preTimeField.setStyle("-fx-border-color: green");
					preTimeSave.set(true);
				} else {
					preTimeField.setStyle("-fx-border-color: red");
					preTimeSave.set(false);

				}

			}
		});

		stationNameField.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
				if (!arg0.getValue().trim().equals("")) {
					stationNameField.setStyle("-fx-border-color: green");
					stationNameSave.set(true);
				} else {
					stationNameField.setStyle("-fx-border-color: red");
					stationNameSave.set(false);
				}
			}
		});

		stationNameSave.addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
				System.out.println(arg0.getValue());

			}
		});

		canSaveStation.bind(stationNameSave.and(preTimeSave).and(postTimeSave));

		canSaveStation.addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {

				if (arg0.getValue())
					saveStationButton.setDisable(false);
				else
					saveStationButton.setDisable(true);

			}
		});

		stationTypeName.textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
				if (!arg0.getValue().trim().equals("")) {
					stationTypeName.setStyle("-fx-border-color: green");
					typeNameSave.set(true);
				} else {
					stationTypeName.setStyle("-fx-border-color: red");
					typeNameSave.set(false);
				}

			}
		});

		typeNameSave.addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
				if (arg0.getValue())
					saveTypeButton.setDisable(false);
				else
					saveTypeButton.setDisable(true);
			}
		});

		stationTypeList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<StationType>() {

			@Override
			public void changed(ObservableValue<? extends StationType> arg0, StationType arg1, StationType arg2) {
				if (arg0.getValue() != null) {
					editTypeButton.setDisable(false);
					deleteTypeButton.setDisable(false);
					setSelectedType(arg0.getValue());
				} else {
					editTypeButton.setDisable(true);
					deleteTypeButton.setDisable(true);
					clearSelectedType();
				}

			}
		});

		stationList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Station>() {

			@Override
			public void changed(ObservableValue<? extends Station> arg0, Station arg1, Station arg2) {
				if (arg0.getValue() != null) {
					editStationButton.setDisable(false);
					deleteStationButton.setDisable(false);
					setSelectedStation(arg0.getValue());

				} else {
					editStationButton.setDisable(true);
					deleteStationButton.setDisable(true);
					clearStationStations();
				}

			}
		});

	}

	private void setSelectedStation(Station stat) {
		stationNameField.setText(stat.getName());
		stationDescriptionArea.setText(stat.getDescription());
		preTimeField.setText(format.format(stat.getPreTime()));
		postTimeField.setText(format.format(stat.getPostTime()));

		typeCheckList.getCheckModel().clearChecks();
		checkTypesOnList(stat.getStationType());
	}

	private void checkTypesOnList(List<StationType> type) {
		ObservableList<StationType> items = typeCheckList.getItems();
		for (int i = 0; i < items.size(); i++) {
			for (StationType st : type)
				if (st.getIDTypu() == items.get(i).getIDTypu())
					typeCheckList.getCheckModel().check(i);
		}
	}

	private void clearStationStations() {
		stationNameField.clear();
		stationDescriptionArea.clear();
		preTimeField.clear();
		postTimeField.clear();
	}

	private void setSelectedType(StationType type) {
		stationTypeName.setText(type.getName());
		stationTypeDescription.setText(type.getDescription());
	}

	private void clearSelectedType() {
		stationTypeName.clear();
		stationTypeDescription.clear();
	}

	// Event Listener on Button[#saveTypeButton].onAction
	@FXML
	public void saveStationType(ActionEvent event) {
		StationType type = new StationType();

		type.setName(stationTypeName.getText().trim());
		type.setDescription(stationTypeDescription.getText().trim());
		type.setStation(null);

		stationTypeName.clear();
		stationTypeDescription.clear();

		try {
			stationTypeService.insertStationType(type);
		} catch (SQLException e) {
			makeAlertWindow();
			e.printStackTrace();
		}

		setTypeList();

	}

	public void setTypeList() {

		typeCheckList.getItems().clear();
		stationTypeList.getItems().clear();

		List<StationType> stationTypes = stationTypeService.getStationTypes();
		types = FXCollections.observableList(stationTypes);

		typeCheckList.setItems(types);
		stationTypeList.setItems(types);
	}

	// Event Listener on Button.onAction
	@FXML
	public void EditStationType(ActionEvent event) {
		StationType selectedItem = stationTypeList.getSelectionModel().getSelectedItem();
		selectedItem.setName(stationTypeName.getText().trim());
		selectedItem.setDescription(stationTypeDescription.getText().trim());

		try {
			stationTypeService.updateStationType(selectedItem);
		} catch (SQLException e) {
			makeAlertWindow();
			e.printStackTrace();
		}

		setTypeList();
	}

	// Event Listener on Button.onAction
	@FXML
	public void DeleteStationType(ActionEvent event) {

		ObservableList<StationType> selectedItems = stationTypeList.getSelectionModel().getSelectedItems();

		try {
			stationTypeService.deleteType(selectedItems);
			stationTypeList.getItems().removeAll(selectedItems);
		} catch (SQLException e) {
			makeAlertWindow();
			e.printStackTrace();
		}
	}

	// Event Listener on Button[#saveStationButton].onAction
	@FXML
	public void saveStation(ActionEvent event) {
		Station stationToSave = saveStation();
		if (stationToSave != null)
			try {
				stationService.insertStation(stationToSave);
				stationService.setTypesForStationInJoinTable(stationToSave);
			} catch (SQLException e) {
				makeAlertWindow();
				e.printStackTrace();
			}

		stationNameField.clear();
		stationDescriptionArea.clear();
		preTimeField.clear();
		postTimeField.clear();

		setStationLists();
	}

	private Station saveStation() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		Station s = new Station();
		try {
			s.setName(stationNameField.getText().trim());
			s.setDescription(stationDescriptionArea.getText().trim());
			s.setPreTime(dateFormat.parse(preTimeField.getText().trim()));
			s.setPostTime(dateFormat.parse(postTimeField.getText().trim()));
			s.setStationType(typeCheckList.getCheckModel().getCheckedItems());
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}

		return s;
	}

	public void setStationLists() {
		List<Station> all = stationService.getAll();
		makeMapOfStations(all);
		stations = FXCollections.observableList(all);
		stationList.getItems().clear();
		stationList.setItems(stations);
	}

	private void makeMapOfStations(List<Station> list) {
		mapOfAllStations = new TreeMap<Long, Station>();

		for (Station s : list)
			mapOfAllStations.put(s.getStationId(), s);

	}

	public static Map<Long, Station> getMapOfStations() {
		return mapOfAllStations;
	}

	@FXML
	public void EditStation(ActionEvent event) {
		Station selectedItem = null;
		try {
			selectedItem = stationList.getSelectionModel().getSelectedItem();
			removeRecordsFromJoinTable(selectedItem);
			selectedItem.setName(stationNameField.getText().trim());
			selectedItem.setDescription(stationDescriptionArea.getText().trim());
			selectedItem.setPreTime(format.parse(preTimeField.getText()));
			selectedItem.setPostTime(format.parse(postTimeField.getText()));
			selectedItem.setStationType(typeCheckList.getCheckModel().getCheckedItems());
			try {
				stationService.updateStationType(selectedItem);
				stationService.setTypesForStationInJoinTable(selectedItem);
			} catch (SQLException e) {
				makeAlertWindow();
				e.printStackTrace();
				setStationLists();
			} 
		}
			catch (ParseException e) {
			e.printStackTrace();
			return;
		}
	}

	private void removeRecordsFromJoinTable(Station station) {
		List<StationType> toRemove = station.getStationType();
		toRemove.removeAll(typeCheckList.getSelectionModel().getSelectedItems());
		try {
			stationService.removeDemandedRecordsFromJoinTable(toRemove, station);
		} catch (SQLException e) {
			makeAlertWindow();
			e.printStackTrace();
		}
	}

	@FXML
	public void DeleteStation(ActionEvent event) {
		try {
			stationService.deleteStation(stationList.getSelectionModel().getSelectedItem());
			setStationLists();
		} catch (SQLException e) {
			makeAlertWindow();
			e.printStackTrace();
		}
	}

	@FXML
	public void CloseWindow(ActionEvent event) {
		stationList.getScene().getWindow().hide();
	}

	private void makeAlertWindow() {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Blad");
		alert.setHeaderText("Nie mozna usunac elementu z bazy.");
		alert.setContentText("Element w bazie jest powiazany z innym.");

		alert.showAndWait();
	}

}
