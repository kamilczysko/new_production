package model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javafx.beans.property.SimpleBooleanProperty;

public class Operation {

	private long id=0;
	private String operationName;
	private Process process;
	private long nextOperationId = 0;
	private Operation nextOperationObject;
	private Long StationTypeId;
	private StationType stationType;
	private Date PreTime;
	private Date PostTime;
	private Date Duration;
	private String description;
	
	private boolean first = false;
	private SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
	private MultiValueMap<Long, Batch> map = new LinkedMultiValueMap<Long, Batch>();
	private SimpleBooleanProperty readyToSave= new SimpleBooleanProperty(false);

	private String group;

	
	
	public Operation(){
		setOperationWithDefaultValues();
	}	
	
	private void setOperationWithDefaultValues(){
		setName("Nowa operacja");
		try {
			setPreTime(format.parse("00:00:10"));
			setPostTime(format.parse("00:00:10"));
			setDuration(format.parse("00:00:10"));
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	public StationType getStationType() {
		return stationType;
	}

	public void setStationType(StationType stationType) {
		this.stationType = stationType;
	}

	public Operation getNextOperationObject() {
		return nextOperationObject;
	}
	
	public void setNextOperationObject(Operation nextOperationObject) {
		this.nextOperationObject = nextOperationObject;
	}

	public SimpleBooleanProperty getReady() {
		return readyToSave;
	}

	public void setReady(boolean ready) {
		this.readyToSave.set(ready);
	}

	public MultiValueMap<Long, Batch> getMap() {
		return map;
	}

	public void setMap(MultiValueMap<Long, Batch> map) {
		this.map = map;
	}

	public Operation(String name){
		setName(name);
		try {
			setPreTime(format.parse("00:00:10"));
			setPostTime(format.parse("00:00:10"));
			setDuration(format.parse("00:00:10"));
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isFirst() {
		return first;
	}

	public void setFirst(boolean first) {
		this.first = first;
	}
	
	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public Long getId() {
		return id;
	}

	public void setId(long iDOperacja) {
		id = iDOperacja;
	}

	public String getName() {
		return operationName;
	}

	public void setName(String nazwa) {
		operationName = nazwa;
	}

	public Process getProces() {
		return process;
	}

	public void setProces(Process proces) {
		this.process = proces;
	}

	public long getNextOperationsId() {
		if(getNextOperationObject() == null)
			return nextOperationId;
		else
			return getNextOperationObject().getId();
	}

	public void setNextOperationsId(long kolejna) {
		nextOperationId = kolejna;
	}

	public Long getStationTypeId() {
		return StationTypeId;
	}

	public void setStationTypeId(Long stationType) {
		StationTypeId = stationType;
	}

	public Date getPreTime() {
		return PreTime;
	}

	public void setPreTime(Date preTime) {
		PreTime = preTime;
	}

	public Date getPostTime() {
		return PostTime;
	}

	public void setPostTime(Date postTime) {
		PostTime = postTime;
	}

	public Date getDuration() {
		return Duration;
	}

	public void setDuration(Date duration) {
		Duration = duration;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String opis) {
		description = opis;
	}
	public boolean isNextOperation(Operation nextOperation){
		System.out.println("next op test: "+this.getNextOperationsId()+" --- "+ nextOperation.getId());
		return this.getNextOperationsId() == nextOperation.getId()? true:false;
	}
	
	@Override
	public String toString() {
		return this.getName();
	}

}
