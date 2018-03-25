package model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Station {

	public Station() {
		possibleDate = null;
	}

	public Station(String name, String description, Date preTime, Date postTime, List<StationType> stationType) {
		possibleDate  = null;
		this.name = name;
		this.description = description;
		this.preTime = preTime;
		this.postTime = postTime;
		this.stationType = stationType;
	}

	private long stationId;
	private String name;
	private String description;
	private Date preTime;
	private Date postTime;

	private List<StationType> stationType;

	private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
//	private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
	
	public long getStationId() {
		return stationId;
	}

	public void setStationId(long stationId) {
		this.stationId = stationId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public List<StationType> getStationType() {
		return stationType;
	}

	public void setStationType(List<StationType> stationType) {
		this.stationType = stationType;
	}

	private String possibleDate = new String();
	
	
	public String getPossibleDate() {
		return possibleDate;
	}
	
	public void setPossibleDate(Date possibleDate) {
		if(possibleDate == null || possibleDate.before(new Date(System.currentTimeMillis())) )
			this.possibleDate = "teraz";
		else
			this.possibleDate = this.dateFormat.format(possibleDate);
	}

	@Override
	public String toString() {
		if(possibleDate != null)
			return getName()+", dostepne: "+getPossibleDate();
		else
			return getName();
	}
}
