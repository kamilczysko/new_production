package controller;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import controller.functions.ColorTableCell;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import model.Container;
import printStuff.PrintContainerBarCode;
import service.ContainerService;

@Controller
public class ContainerController {

	@FXML
	private TableColumn<Container, Date> deleteDateColumn;
	@FXML
	private TableColumn<Container, Short> signColumn;
	@FXML
	private TableColumn<Container, Color> colorColumn;
	@FXML
	private TableView<Container> table;
	@FXML
	private TableColumn<Container, Short> idColumn;
	@FXML
	private TableColumn<Container, Date> createDateColumn;
	@FXML
	private TableColumn<Container, Short> deletedMark;
	@FXML
	private TableView<Container> tableWithDeleted;
	@FXML
	private TableColumn<Container, Date> deletedCreateDate;
	@FXML
	private TableColumn<Container, Date> deletedDeleteDate;
	@FXML
	private TableColumn<Container, Short> deletedId;
	@FXML
	private TableColumn<Container, Color> deletedColor;

	@Autowired
	private ContainerService containerService;
	
	private List<Container> containers;

	@FXML
	private void initialize() {
		
		containers = new ArrayList<Container>();

		table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		deleteDateColumn.setCellValueFactory(new PropertyValueFactory<Container, Date>("dataKasacji"));
		createDateColumn.setCellValueFactory(new PropertyValueFactory<Container, Date>("dataUtworzenia"));
		colorColumn.setCellValueFactory(new PropertyValueFactory<Container, Color>("color"));
		colorColumn.setCellFactory(ColorTableCell<Container>::new);
		signColumn.setCellValueFactory(new PropertyValueFactory<Container, Short>("oznaczenie"));
		idColumn.setCellValueFactory(new PropertyValueFactory<Container, Short>("id"));

		tableWithDeleted.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		deletedDeleteDate.setCellValueFactory(new PropertyValueFactory<Container, Date>("dataKasacji"));
		deletedCreateDate.setCellValueFactory(new PropertyValueFactory<Container, Date>("dataUtworzenia"));
		deletedColor.setCellValueFactory(new PropertyValueFactory<Container, Color>("color"));
		deletedMark.setCellValueFactory(new PropertyValueFactory<Container, Short>("oznaczenie"));
		deletedId.setCellValueFactory(new PropertyValueFactory<Container, Short>("id"));

		containers.addAll(getRecords());
		
		fillTableWithContainers();
	}
	
	private void fillTableWithContainers() {
		table.getItems().clear();
		table.getItems().addAll(this.containers);
		
		tableWithDeleted.getItems().clear();
		tableWithDeleted.getItems().addAll(getDeletedContainers());
	}

	private List<Container> getRecords() {
		return containerService.getAllContainers();
	}

	private List<Container> getDeletedContainers() {
		return containerService.getAllDeletedContainers();
	}

	@FXML
	public void deleteContainer(ActionEvent event) {
		ObservableList<Container> selectedItems = table.getSelectionModel().getSelectedItems();
//		System.out.println(selectedItems.size());
		setRemoved(selectedItems);
	}

	private void setRemoved(List<Container> containerList) {
		try {
			containerService.removeListOfContainers(containerList);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		fillTableWithContainers();
	}

	@FXML
	public void saveContainser(ActionEvent event) {
		ObservableList<Container> items = table.getItems();
		try {
			containerService.save(items);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		fillTableWithContainers();
	}

	private Container makeNewContainer() {
		Container container = new Container();
		short markForContainer = getLastGoodMark();
		markForContainer++;
		container.setOznaczenie(markForContainer);

		return container;
	}

	private short getLastGoodMark() {
		ObservableList<Container> tableItems = table.getItems();
		ListIterator<Container> tableItemsIterator = tableItems.listIterator();

		Container previous = null;
		while (tableItemsIterator.hasNext()) {
			Container actual = tableItemsIterator.next();
			if (previous != null) {
				if ((actual.getOznaczenie() - previous.getOznaczenie()) > 1)
					return previous.getOznaczenie();
			} else if (actual.getOznaczenie() > 1)
				return 0;

			previous = actual;
		}
		if (previous != null)
			return previous.getOznaczenie();
		else
			return 0;
	}

	@FXML
	public void addContainer() {
		Container newContainer = makeNewContainer();
		containers.add(newContainer);
		containers.sort((a,b)-> a.getOznaczenie() - b.getOznaczenie());
		
		fillTableWithContainers();
	}

	@FXML
	private void print() {
		ObservableList<Container> selectedItems = table.getSelectionModel().getSelectedItems();
		String[][] listToPrint = new String[selectedItems.size()][1];
		int index = 0;
		for (Container c : selectedItems) {
			listToPrint[index][0] = String.format("%03X",c.getOznaczenie())+"";
			index++;
		}
		new PrintContainerBarCode(0, 0, listToPrint);
	}
}
