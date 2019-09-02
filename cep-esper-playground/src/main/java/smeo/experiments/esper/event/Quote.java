package smeo.experiments.esper.event;

import java.io.Serializable;

/**
 * Created by truehl on 25.07.16.
 */
public class Quote implements Serializable {
	public void setBank(String bank) {
		this.bank = bank;
	}

	public void setStreamName(String streamName) {
		this.streamName = streamName;
	}

	public void setAskRate(double askRate) {
		this.askRate = askRate;
	}

	public void setBidRate(double bidRate) {
		this.bidRate = bidRate;
	}

	public void setLiquidity(double liquidity) {
		this.liquidity = liquidity;
	}

	public String bank;
	public String streamName;
	public double askRate;
	public double bidRate;
	public double liquidity;

	public Quote(String bank, String streamName, double askRate, double bidRate, double liquidity) {
		this.bank = bank;
		this.streamName = streamName;
		this.askRate = askRate;
		this.bidRate = bidRate;
		this.liquidity = liquidity;
	}

	public String getBank() {
		return bank;
	}

	public String getStreamName() {
		return streamName;
	}

	public double getAskRate() {
		return askRate;
	}

	public double getBidRate() {
		return bidRate;
	}

	public double getLiquidity() {
		return liquidity;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		Quote quote = (Quote) o;

		if (Double.compare(quote.askRate, askRate) != 0)
			return false;
		if (Double.compare(quote.bidRate, bidRate) != 0)
			return false;
		if (Double.compare(quote.liquidity, liquidity) != 0)
			return false;
		if (!bank.equals(quote.bank))
			return false;
		return streamName != null ? streamName.equals(quote.streamName) : quote.streamName == null;

	}

	@Override
	public int hashCode() {
		int result;
		long temp;
		result = bank.hashCode();
		result = 31 * result + (streamName != null ? streamName.hashCode() : 0);
		temp = Double.doubleToLongBits(askRate);
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(bidRate);
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(liquidity);
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public String toString() {
		return "Quote{" + "bank='" + bank + '\'' + ", stream='" + streamName + '\'' + ", askQuote=" + askRate + ", bidQuote=" + bidRate + ", liquidity="
				+ liquidity + '}';
	}
}
