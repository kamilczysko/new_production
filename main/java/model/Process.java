package model;

public class Process {

	private long IDProces;
	private int article;
	private String name;
	private String description;
	private short batch;

	public long getIDProces() {
		return IDProces;
	}

	public void setIDProces(long iDProces) {
		IDProces = iDProces;
	}

	public int getArticle() {
		return article;
	}

	public void setArticle(int article) {
		this.article = article;
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

	public short getBatch() {
		return batch;
	}

	public void setBatch(short batch) {
		this.batch = batch;
	}
	
	@Override
	public String toString() {
		return this.getName()+" - "+this.getIDProces()+" - "+this.getArticle() + " - "+this.getBatch();
	}

}
