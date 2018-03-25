package controller.functions;

import java.text.SimpleDateFormat;
import java.util.Date;

import model.Operation;
import model.Station;

public class ReportObject {
	
	private Station station;
	private Date start, end;
	private String problems;
	private Operation operation;
	private int amount;
	private String operator;
	
	private SimpleDateFormat dayFormat = new SimpleDateFormat("dd.MM.yyyy");
	private SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm:ss");
		
	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getDate(){
		return dayFormat.format(getStart());
	}
	
	public String getEndString(){
		return hourFormat.format(getEnd());
	}
	public String getStartString(){
		return hourFormat.format(getStart());
	}
	
	public Station getStation() {
		return station;
	}
	public void setStation(Station station) {
		this.station = station;
	}
	public Date getStart() {
		return start;
	}
	public void setStart(Date start) {
		this.start = start;
	}
	public Date getEnd() {
		return end;
	}
	public void setEnd(Date end) {
		this.end = end;
	}
	public String getProblems() {
		return problems;
	}
	public void setProblems(String problems) {
		this.problems = problems;
	}
	public Operation getOperation() {
		return operation;
	}
	public void setOperation(Operation operation) {
		this.operation = operation;
	}
	public int getAmount() {
		return amount;
	}
	public void setAmount(int amount) {
		this.amount = amount;
	}
	
	@Override
	public String toString() {
		return getStation().getName();
	}
	
	
	

}
