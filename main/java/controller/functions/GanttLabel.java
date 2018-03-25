package controller.functions;

import java.util.Date;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import model.ChartDataObject;

public class GanttLabel extends Label {

	private long zeroReference;
	private int offset = 0;
	private long orderId;

	private String pracownik;
	private String opis;
	private String stanowiskoOrazId;

	public GanttLabel() {
		this.setAlignment(Pos.CENTER);
	}

	public void setOrderId(long orderId) {
		this.orderId = orderId;
	}

	public void setParams(ChartDataObject t, long zero, int factor, int offset) {
		this.setPrefHeight(50.0);
		this.getStyleClass().add("labsiorek");
		this.offset = offset;
		this.zeroReference = zero;
		setInfo(t);
	}

	private void setInfo(ChartDataObject t) {
		setInfos(t);
	}

	private void setInfos(ChartDataObject t) {
		this.pracownik = t.getWorker();
		this.opis = t.getDescription();
		this.stanowiskoOrazId = t.getStationWithId();
		this.setDimensions(t);

		this.setText(orderId+" - " + t.getBatchNumber() + " \n" + this.pracownik);
		Tooltip toolTip = new Tooltip();
		toolTip.setText(stanowiskoOrazId + "\n" + "Liczba sztuk: " + t.getAmount() + "\n" + t.getOperationName());
		this.setTooltip(toolTip);
	}

	private void setDimensions(ChartDataObject t) {

		if (t.getEnd() == null)
			this.setStyle("-fx-background-color: " + t.getColor());
		Date start = t.getStart();
		long duration;

		if (t.getEnd() == null || t.getEnd().getTime() < 0) {
			duration = t.getDurationInSeconds();
			if (t.getEnd() == null)
				this.getStyleClass().add("labsiorek");
			else
				this.getStyleClass().add("labsiorek-started");

		} else {
			duration = (t.getEnd().getTime() - t.getStart().getTime()) / 1000;
			this.getStyleClass().add("labsiorek-done");
		}
		this.setPrefWidth(duration);
		this.setTranslateX(getXpos(start));
	}

	private double getXpos(Date start) {
		return ((start.getTime() / 1000) - zeroReference + offset);
	}

	public void changeFactor(double factor) {
		this.setPrefWidth(this.getPrefWidth() * factor);
		this.setTranslateX((this.getTranslateX() - offset) * factor + offset);
	}

	public void setPosY(double yPos) {
		this.setTranslateY(yPos);
	}

	public String getWorker() {
		return this.pracownik;
	}

	public String getDescription() {
		return opis;
	}

}
