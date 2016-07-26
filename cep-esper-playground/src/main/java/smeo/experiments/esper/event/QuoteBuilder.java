package smeo.experiments.esper.event;

public class QuoteBuilder {
	private String bank;
	private String streamName = "default";
	private double askRate;
	private double bidRate;
	private double liquidity;

	public QuoteBuilder(String bank) {
		this.bank = bank;
	}

	public QuoteBuilder withStreamName(String streamName) {
		this.streamName = streamName;
		return this;
	}

	public QuoteBuilder askRate(double askRate) {
		this.askRate = askRate;
		return this;
	}

	public QuoteBuilder bidRate(double bidRate) {
		this.bidRate = bidRate;
		return this;
	}

	public QuoteBuilder liquidity(double liquidity) {
		this.liquidity = liquidity;
		return this;
	}

	public Quote create() {
		return new Quote(bank, streamName, askRate, bidRate, liquidity);
	}
}