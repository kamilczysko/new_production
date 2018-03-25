package model;

public class Orders {

	private int orderId;
	private int amount;
	private String description;

	private Article article;
	private Integer orderInProgress;
	private String number;

	public Orders() {
	}

	public int getOrderId() {
		return orderId;
	}

	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getOrderInProgress() {
		return orderInProgress;
	}
	
	public boolean isInProgress() {
		return (getOrderInProgress() != null) && getOrderInProgress() == 1 ? true : false;

	}

	public void setOrderInProgress(Integer orderInProgress) {
		this.orderInProgress = orderInProgress;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public Article getArticle() {
		return article;
	}

	public void setArticle(Article article) {
		this.article = article;
	}
	
	@Override
	public String toString(){
		if(getOrderInProgress() == 1)
			return article.getName()+" - "+this.getOrderId()+ " *";
		else
			return article.getName()+" - "+this.getOrderId();
		
	}

}
