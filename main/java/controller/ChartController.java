package controller;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import application.WorkingHoursConstants;
import controller.functions.GanttChart;
import controller.functions.MyDate;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import model.ChartDataObject;
import model.Orders;
import service.ChartDataService;
import service.OrdersService;

@Controller
public class ChartController {
	@FXML
	private ComboBox<Orders> orderBox;
	@FXML
	private ScrollPane scrollPane;
	@FXML
	private Label dateLabel;
	@FXML
	private ComboBox<MyDate> setDayOnChart;

	@Autowired
	private OrdersService ordersService;
	@Autowired
	private ChartDataService dataService;

	private final String[] colors = { // kolory operacji na wykresie
			"#528FFF", "#65C798", "#9E71A7", "#FDEBA9", "#BC7C03", "#FF7C56", "#968BCA", "#9DDC85", "#877BAE" };
	private Map<Long, String> colorsForOperations;
	private short colorIndex = 0;

	private Orders selectedOrder = null;
	private List<ChartDataObject> dataForOrder;

	private GanttChart gantt;

	@FXML
	private void initialize() {
		setOrdersList();
		setDateComboListener();
	}

	private void setDateComboListener() {
		setDayOnChart.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<MyDate>() {

			@Override
			public void changed(ObservableValue<? extends MyDate> arg0, MyDate arg1, MyDate arg2) {
				try {
					if (setDayOnChart.getItems().size() > 1) {
						List<ChartDataObject> selectedDay = selectOnlyInGivenDay(arg0.getValue());
						makeChart(selectedDay);
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

		});
	}

	private List<ChartDataObject> selectOnlyInGivenDay(MyDate dates) {
		List<ChartDataObject> list = new ArrayList<ChartDataObject>();
		MyDate[] range = getDateRange(dates);
		for (ChartDataObject obj : dataForOrder)
			if ((obj.getStart().after(range[0].getMyDate()) || obj.getStart().equals(range[0].getMyDate()))
					&& obj.getStart().before(range[1].getMyDate()))
				list.add(obj);

		return list;
	}

	private MyDate[] getDateRange(MyDate startDate) {
		MyDate[] range = new MyDate[2];
		if (startDate != null) {
			MyDate date1 = new MyDate();
			date1.setTime(startDate.getTime());
			date1.setHourAndMinute(WorkingHoursConstants.WORK_START_HOUR,
					WorkingHoursConstants.WORK_START_MINUTES - WorkingHoursConstants.TIME_MARGIN / 60);
			MyDate date2 = new MyDate();
			date2.setTime(startDate.getTime());
			date2.setHourAndMinute(WorkingHoursConstants.WORK_END_HOUR, WorkingHoursConstants.WORK_END_MINUTES);

			range[0] = date1;
			range[1] = date2;
		}
		return range;
	}

	public void setOrdersList() {
		List<Orders> orders = ordersService.getInProgressOrders();
		orderBox.getSelectionModel().clearSelection();
		orderBox.getItems().clear();
		orderBox.getItems().addAll(orders);
	}

	public void clearPane() {
		scrollPane.setContent(null);
	}

	@FXML
	public void selectOrder(ActionEvent event) {

		try {
			this.dataForOrder = getDataForOrder();
			makeDays(dataForOrder);
			makeChart(this.dataForOrder);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private List<ChartDataObject> getDataForOrder() throws SQLException {
		if (orderBox.getSelectionModel().getSelectedItem() != null) {
			this.selectedOrder = orderBox.getSelectionModel().getSelectedItem();
			return dataService.getDataForOrder(this.selectedOrder.getOrderId());
		}
		return new ArrayList<ChartDataObject>();
	}

	private void makeChart(List<ChartDataObject> list) throws SQLException {
		List<ChartDataObject> timings = new ArrayList<ChartDataObject>();
		this.colorsForOperations = new TreeMap<Long, String>();
		this.colorIndex = 0;

		timings.addAll(list);
		// makeDays(timings);
		setColorForOperation(timings);
		this.gantt = initGanttChart(timings);
		scrollPane.setContent(gantt);

	}

	private GanttChart initGanttChart(List<ChartDataObject> timings) {
		gantt = new GanttChart();
		gantt.setOrderId(this.selectedOrder.getOrderId());
		gantt.setDateLabelToExpressDate(dateLabel);
		gantt.initChart(timings);

		return gantt;

	}

	private void makeDays(List<ChartDataObject> timings) {
		setDayOnChart.getItems().clear();
		List<MyDate> days = new ArrayList<MyDate>();
		boolean con = false;
		for (ChartDataObject dataObj : timings) {
			Date start = dataObj.getStart();
			MyDate date = new MyDate();
			date.setTime(start.getTime());
			date.setHourAndMinute(7, 0);
			for (MyDate mdate : days)
				if (mdate.getDay() == date.getDay()) {
					con = true;
					break;
				}
			if (!con)
				days.add(date);
			con = false;

		}
		days.sort(new Comparator<MyDate>() {

			@Override
			public int compare(MyDate o1, MyDate o2) {
				return o1.compareTo(o2);
			}
		});
		setDayOnChart.getItems().addAll(days);
	}

	private void setColorForOperation(List<ChartDataObject> dataObject) {

		for (ChartDataObject t : dataObject) {
			long idOperacja = t.getIdOperation();
			if (colorsForOperations.containsKey(idOperacja)) {
				t.setColor(colorsForOperations.get(idOperacja));
			} else {
				colorsForOperations.put(idOperacja, colors[colorIndex]);
				t.setColor(colors[colorIndex]);

				if (colorIndex == colors.length - 1)
					colorIndex = 0;
				else
					colorIndex++;
			}
		}
	}

	private double factor = 1.0;

	@FXML
	private void bigger() {
		factor = 1.5;
		if (factor != 0)
			gantt.setLabFactor(factor);
	}

	@FXML
	private void smaller() {
		factor = 0.5;
		if (factor != 0)
			gantt.setLabFactor(factor);
	}

	@FXML
	private void refresh() {
		try {
			this.dataForOrder = getDataForOrder();
			makeChart(this.getDataForOrder());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@FXML
	void setWholeChart(ActionEvent event) throws SQLException {
		makeChart(this.getDataForOrder());
	}

	@FXML
	void fitChart(ActionEvent event) {
		gantt.scale();
	}
}
