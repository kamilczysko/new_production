package model;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.List;

public class Production {

	private long IDProdukcja;
	private long batch;
	private long operation;
	private long station;
	
//	private short amount;
	private Operation operationObject;
	private Station stationObject;
	private Batch batchObject;
	
	private String PreTime;
	private String PostTime;
	private String description;

	private List<Timing> timing;
//	private String group;
	private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
	
	public String getGroup(){
		return batchObject.getGrupa();
	}
	
	public long getDurationInSeconds(){//zwraca czas trwanai operacji w sekundach
		int amount = batchObject.getIlosc();
		long pretime = getInSeconds(operationObject.getPreTime());
		long postTime = getInSeconds(operationObject.getPostTime());
		long duration = getInSeconds(operationObject.getDuration());
		long wholeTime = (pretime + postTime + (amount * duration));// w sekundach
		System.out.println(this+" -duration --- "+wholeTime+" preTime"+operationObject.getPreTime()+" post"+operationObject.getPostTime()+" dur"+operationObject.getDuration());
		return wholeTime;
	}
	
	public long getDurationOfSingleOperation(){
		return getInSeconds(operationObject.getDuration());
	}
	
	private long getInSeconds(Date input){//czas poddany w formacie HH:mm:ss konwertowany jest na sekuny
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		String time = dateFormat.format(input);
		String[] vals = time.split(":");
		Duration duration = Duration.parse(String.format("PT%sH%sM%sS", vals[0],vals[1],vals[2]));

		return duration.getSeconds();
	}
	
	

	public long getBatch() {
		return batch;
	}

	public void setBatch(long batch) {
		this.batch = batch;
	}

	public long getOperation() {
		return operation;
	}

	public void setOperation(long operation) {
		this.operation = operation;
	}

	public long getStation() {
		return station;
	}

	public void setStation(long station) {
		this.station = station;
	}

	public Operation getOperationObject() {
		return operationObject;
	}

	public void setOperationObject(Operation operationObject) {
		this.operationObject = operationObject;
	}

	public Station getStationObject() {
		return stationObject;
	}

	public void setStationObject(Station stationObject) {
		this.stationObject = stationObject;
	}

	public Batch getBatchObject() {
		return batchObject;
	}

	public void setBatchObject(Batch batchObject) {
		this.batchObject = batchObject;
	}

	public SimpleDateFormat getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(SimpleDateFormat dateFormat) {
		this.dateFormat = dateFormat;
	}

//	public void setGroup(String group) {
//		this.group = group;
//	}

	public List<Timing> getTiming() {
		return timing;
	}

	public void setTiming(List<Timing> timing) {
		this.timing = timing;
	}

	public long getIDProdukcja() {
		return IDProdukcja;
	}

	public void setIDProdukcja(long iDProdukcja) {
		IDProdukcja = iDProdukcja;
	}

	public String getPreTime() {
		return PreTime;
	}

	public void setPreTime(String preTime) {
		PreTime = preTime;
	}

	public String getPostTime() {
		return PostTime;
	}

	public void setPostTime(String postTime) {
		PostTime = postTime;
	}

	
	public long getPreTimeInSeconds() {
			System.out.println("preTime: "+ getPreTime());
			return getInSeconds(getOperationObject().getPreTime());
	}	
	public long getPostTimeInSeconds(){
			System.out.println("postTime: "+ getPostTime());
			return getInSeconds(getOperationObject().getPostTime());
	}	
	
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	@Override
	public String toString() {
		return "produkcja--- operacja:"+this.getOperation()+", stanowisko:"+this.getStation()+", partia: "+this.getBatch();
	}

}
