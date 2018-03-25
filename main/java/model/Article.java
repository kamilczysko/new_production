package model;

import java.util.List;

public class Article {

	private int id;
	private String name;
	private String nameCont;
	private String barCode;
	private String katalogIdx;
	private String description;
	private List<Process> processList;
	
	
	
	public List<Process> getProcessList() {
		return processList;
	}

	public void setProcessList(List<Process> processList) {
		this.processList = processList;
	}

	public int getId() {
		return id;
	}

	public void setId(int i) {
		this.id = i;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNameCont() {
		return nameCont;
	}

	public void setNameCont(String nameCont) {
		this.nameCont = nameCont;
	}

	public String getBarCode() {
		return barCode;
	}

	public void setBarCode(String barCode) {
		this.barCode = barCode;
	}

	public String getKatalogIdx() {
		return katalogIdx;
	}

	public void setKatalogIdx(String katalogIdx) {
		this.katalogIdx = katalogIdx;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
