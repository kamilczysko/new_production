package model;

import java.util.List;


public class StationType {

	public StationType() {
		this.setIDTypu(-1);
	}

	public StationType(String name, String description) {
		this.setIDTypu(-1);
		this.name = name;
		this.description = description;
	}

	private long IDTypu;

	private String name;
	private String description;

	private List<Station> station;

	public long getIDTypu() {
		return IDTypu;
	}

	public void setIDTypu(long iDTypu) {
		IDTypu = iDTypu;
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

	public List<Station> getStation() {
		return station;
	}

	public void setStation(List<Station> station) {
		this.station = station;
	}

	@Override
	public String toString() {
		return this.getName();
	}

}
