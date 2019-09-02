package smeo.experiments.esper.priceaggregation.domain.event;

/**
 * Created by truehl on 22.07.16.
 */
public class PriceEvent {
	private final long id;
	private final String streamName;
	private final String bank;
	private final double bidPrice;
	private final double askPrice;

	public PriceEvent(long id, String streamName, String bank, double bidPrice, double askPrice) {
		this.id = id;
		this.streamName = streamName;
		this.bank = bank;
		this.bidPrice = bidPrice;
		this.askPrice = askPrice;
	}

	public long getId() {
		return id;
	}

	public String getStreamName() {
		return streamName;
	}

	public String getBank() {
		return bank;
	}

	public double getBidPrice() {
		return bidPrice;
	}

	public double getAskPrice() {
		return askPrice;
	}
}
