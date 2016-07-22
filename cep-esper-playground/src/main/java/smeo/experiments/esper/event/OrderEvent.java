package smeo.experiments.esper.event;

/**
 * Created by truehl on 22.07.16.
 */
public class OrderEvent {
	private String itemName;
	private double price;

	public OrderEvent(String itemName, double price) {
		this.itemName = itemName;
		this.price = price;
	}

	public String getItemName() {
		return itemName;
	}

	public double getPrice() {
		return price;
	}
}
