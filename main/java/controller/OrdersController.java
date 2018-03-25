package controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import model.Orders;
import service.OrdersService;

@Controller
public class OrdersController {
	@FXML
	private ComboBox<Orders> orderBox;
	@FXML
	private Label artNameLabel;
	@FXML
	private Label artAmountLabel;
	@FXML
	private TextArea artDescriptionField;

	private Orders selectedItem;

	@Autowired
	private MainWindowController mainWindowController;
	@Autowired
	private OrdersService orderService;
	@Autowired
	private ProcessController processController;
	@Autowired
	private OperationsController operationsController;

	private boolean isLoading = false;

	@FXML
	private void initialize() {
		initStuff();
	}

	public void initStuff() {
		setOrdersListInComboBox();
	}

	public void setOrdersListInComboBox() {// ustawienie listy z zamownieniami
		isLoading = true;
		clearTextFields();
		orderService.makeProcessMap();
		List<Orders> list = orderService.getOrders();
		ObservableList<Orders> orders = FXCollections.observableList(list);
		orderBox.getItems().clear();
		orderBox.getItems().addAll(orders);
		isLoading = false;
	}

	@FXML
	private void selectedItem(ActionEvent event) {
		if (!isLoading) {
			clearThingsAfterSelection();
			this.selectedItem = orderBox.getSelectionModel().getSelectedItem();
			lockProcessTabIfNeeded();
			setInformationsInTextFields();
			processController.setProcessOnList();
			setTitleBar();
		}
	}

	private void setTitleBar() {
		Stage stage = (Stage) artNameLabel.getScene().getWindow();
		stage.setTitle("Hills - " + this.selectedItem.getOrderId() + " - " + selectedItem.getArticle().getName());
	}

	private void clearThingsAfterSelection() {
		processController.clearTable();
		operationsController.clearPane();
		clearTextFields();
	}

	private void lockProcessTabIfNeeded() {
		if (isSelectedOrderInProgress())
			mainWindowController.lockProcessTab(true);
		else
			mainWindowController.lockProcessTab(false);
	}

	private boolean isSelectedOrderInProgress() {
		return selectedItem != null && this.selectedItem.isInProgress() ? true : false;
	}

	private void setInformationsInTextFields() {
		artNameLabel.setText(selectedItem.getArticle().getName());
		artAmountLabel.setText(selectedItem.getAmount() + "");
		artDescriptionField.setText(selectedItem.getDescription());
	}

	public Orders getSelectedOrder() {
		return selectedItem;
	}

	public int getArticleID() {
		if (selectedItem != null)
			return selectedItem.getArticle().getId();
		else
			return -999;
	}

	public int getOrderAmount() {
		if (selectedItem != null)
			return selectedItem.getAmount();
		else
			return 0;
	}

	public int getOrderID() {
		if (selectedItem != null)
			return selectedItem.getOrderId();
		else
			return -999;
	}

	private void clearTextFields() {
		artNameLabel.setText("");
		artAmountLabel.setText("");
		artDescriptionField.setText("");
	}
}
