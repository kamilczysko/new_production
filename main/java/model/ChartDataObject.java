package model;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;

import controller.functions.MyDate;

public class ChartDataObject {

	private MyDate start, end;
	private int amount;
	private boolean isDone = false;
	private String description;
	private Date preTime, postTime, duration;
	private String stationWithId, worker, operationName;
	private int batchNumber;
	private long idOperation;
	private String color;

//	private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

	public ChartDataObject() {
	}

	public ChartDataObject(Builder b) {

		if (b == null)
			return;

		start = b.start;
		end = b.end;
		amount = b.amount;
		isDone = b.isDone;
		description = b.description;
		preTime = b.preTime;
		postTime = b.postTime;
		duration = b.duration;
		stationWithId = b.stationWithId;
		worker = b.worker;
		operationName = b.operationName;
		batchNumber = b.batchNumber;
		idOperation = b.idOperation;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public long getIdOperation() {
		return idOperation;
	}

	public void setIdOperation(long idOperation) {
		this.idOperation = idOperation;
	}

	public MyDate getStart() {
		return start;
	}

	public void setStart(MyDate start) {
		this.start = start;
	}

	public MyDate getEnd() {
		return end;
	}

	public void setEnd(MyDate end) {
		this.end = end;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public boolean isDone() {
		return isDone;
	}

	public void setDone(boolean isDone) {
		this.isDone = isDone;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getPreTime() {
		return preTime;
	}

	public void setPreTime(Date preTime) {
		this.preTime = preTime;
	}

	public Date getPostTime() {
		return postTime;
	}

	public void setPostTime(Date postTime) {
		this.postTime = postTime;
	}

	public Date getDuration() {
		return duration;
	}

	public void setDuration(Date duration) {
		this.duration = duration;
	}

	public String getStationWithId() {
		return stationWithId;
	}

	public void setStationWithId(String stationWithId) {
		this.stationWithId = stationWithId;
	}

	public String getWorker() {
		return worker;
	}

	public void setWorker(String pracownik) {
		this.worker = pracownik;
	}

	public String getOperationName() {
		return operationName;
	}

	public void setOperationName(String operationName) {
		this.operationName = operationName;
	}

	public int getBatchNumber() {
		return batchNumber;
	}

	public void setBatchNumber(int batchNumber) {
		this.batchNumber = batchNumber;
	}

	public long getDurationInSeconds() {
		long wholeTime = -1;
		int amount = this.getAmount();
		long pretime = getInSeconds(this.getPreTime());
		long postTime = getInSeconds(this.getPostTime());
		long duration = getInSeconds(this.getDuration());
		
		wholeTime = (pretime + postTime + (amount * duration));// w sekundach
		return wholeTime;
	}

	private long getInSeconds(Date input) {// czas poddany w formacie HH:mm:ss
											// konwertowany jest na sekuny
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		String time = dateFormat.format(input);
		String[] vals = time.split(":");
		Duration duration = Duration.parse(String.format("PT%sH%sM%sS", vals[0], vals[1], vals[2]));

		return duration.getSeconds();
	}

	public static class Builder {
		private MyDate start, end;
		private int amount;
		private boolean isDone = false;
		private String description;
		private Date preTime, postTime, duration;
		private String stationWithId, worker, operationName;
		private int batchNumber;
		private long idOperation;
		
		public Builder Start(MyDate start) {
			this.start = start;
			return this;
		}

		public Builder End(MyDate end) {
			this.end = end;
			return this;
		}

		public Builder Amount(int amount) {
			this.amount = amount;
			return this;
		}

		public Builder Done(boolean isDone) {
			this.isDone = isDone;
			return this;
		}

		public Builder Description(String description) {
			this.description = description;
			return this;
		}

		public Builder PreTime(Date preTime) {
			this.preTime = preTime;
			return this;
		}

		public Builder PostTime(Date postTime) {
			this.postTime = postTime;
			return this;
		}

		public Builder Duration(Date duration) {
			this.duration = duration;
			return this;
		}

		public Builder StationWithId(String stationWithId) {
			this.stationWithId = stationWithId;
			return this;
		}

		public Builder Worker(String pracownik) {
			this.worker = pracownik;
			return this;
		}

		public Builder OperationName(String operationName) {
			this.operationName = operationName;
			return this;
		}

		public Builder BatchNumber(int batchNumber) {
			this.batchNumber = batchNumber;
			return this;
		}

		public ChartDataObject build() {
			return new ChartDataObject(this);
		}
		
		public Builder OperationId(long idOperation) {
			this.idOperation = idOperation;
			return this;
		}

	}

}
