package model;

import java.util.Date;

public class Timing {

	private long id;
	private String worker;
	private Date start;
	private Date end;
	private short amount;
	private short lacks;
	private Container container;
	

	private Production production;
	private String color;
	
	public Timing(Timing t) {
		setAmount(t.getAmount());
		setOpis(t.getOpis());
		setProduction(t.getProduction());
		setStart(t.getStart());
	}

	public Timing() {

	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}
	
	private String Opis;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Production getProduction() {
		return production;
	}

	public void setProduction(Production production) {
		this.production = production;
		if (production != null) 
			setId(production.getIDProdukcja());
	}

	public String getWorker() {
		return worker;
	}

	public void setWorker(String worker) {
		this.worker = worker;
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

	public short getAmount() {
		return amount;
	}

	public void setAmount(short amount) {
		this.amount = amount;
	}

	public short getLacks() {
		return lacks;
	}

	public void setLacks(short lacks) {
		this.lacks = lacks;
	}

	public String getOpis() {
		return Opis;
	}

	public void setOpis(String opis) {
		Opis = opis;
	}

	@Override
	public String toString() {
		return this.getId() + " -- timing\n";
	}

	public Container getContainer() {
		return container;
	}

	public void setContainer(Container container) {
		this.container = container;
	}

	public Date getCalculatedEndTime(){
		
		return new Date(getStart().getTime() + getProduction().getDurationInSeconds()*1000);
	}

	public Date calculateEndDate(){
		return new Date(getStart().getTime() + getDurationOfWholeProductionInSeconds()*1000);
	}

	public long getPostTimeInSeconds() {
		return getProduction().getPostTimeInSeconds();
	}

	public long getPreTimeInSeconds() {
		return getProduction().getPreTimeInSeconds();
	}

	public long getDurationOfWholeProductionInSeconds() {
		return getProduction().getDurationInSeconds();
	}

	public long getDurationOfThisActualTiming(){
		return getProduction().getDurationOfSingleOperation()*getAmount();
	}
	
	public long getDurationOfSingleOperationInSeconds() {
		long single = (getDurationOfWholeProductionInSeconds() - getPreTimeInSeconds() - getPostTimeInSeconds())/getAmount();
		return single;
	}

}
