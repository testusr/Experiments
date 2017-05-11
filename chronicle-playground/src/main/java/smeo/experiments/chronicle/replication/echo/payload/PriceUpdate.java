package smeo.experiments.chronicle.replication.echo.payload;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Reprensents the price update for a single currency couple in a stream
 */
public class PriceUpdate implements Externalizable {
	public static final double NO_AMOUNT = -1.0;
	public static final double NO_PRICE = -1.0;

	public static final boolean NON_EXCLUSIVE = false;
	public static final boolean EXCLUSIVE = true;

	public static final boolean AGGREGATEABLE_BANDS = true;
	public static final boolean NONAGGREGATEABLE_BANDS = false;

	private static AtomicLong priceIdSequence = new AtomicLong(0);

	private EfficientStreamId streamId;
	private CurrencyCouple currencyCouple;

	private boolean updateChangedPrice = false;

	/**
	 * If the liquidity "amounts" of several price bands are allowed to be aggregated with each other to build a price
	 */
	public boolean hasAggregateablePriceBands;

	// business id assigned by the external price creating unit to be traced back in external systems
	public long id;

	// these two ids identify what the transformation (transformationId) was and which engine created the transformation
	// we could have two or more engines performing the same transformation "racing against each other" to only use
	// the fastest least lagging result.

	// identifier of the transformation that has created this price
	public int transformationId;

	private int noOfPriceBands = 0;

	private double[] amounts;
	private double[] askRates;
	private double[] bidRates;

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeBoolean(updateChangedPrice);
		streamId.writeExternal(out);
		currencyCouple.writeExternal(out);
		out.writeInt(transformationId);
		out.writeLong(id);
		out.writeBoolean(hasAggregateablePriceBands);
		out.writeInt(noOfPriceBands);
		assert noOfPriceBands <= amounts.length;
		for (int i = 0; i < noOfPriceBands; i++) {
			out.writeDouble(amounts[i]);
			out.writeDouble(askRates[i]);
			out.writeDouble(bidRates[i]);
		}
	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		updateChangedPrice = in.readBoolean();
		streamId.readExternal(in);
		currencyCouple.readExternal(in);
		transformationId = in.readInt();
		id = in.readLong();
		hasAggregateablePriceBands = in.readBoolean();
		noOfPriceBands = in.readInt();
		resizeArraysIfNecessary();
		for (int i = 0; i < noOfPriceBands; i++) {
			amounts[i] = in.readDouble();
			askRates[i] = in.readDouble();
			bidRates[i] = in.readDouble();
		}
	}

	private void resizeArraysIfNecessary() {
		if (amounts == null || noOfPriceBands >= amounts.length) {
			double[] newAmounts = new double[noOfPriceBands + 5];
			double[] newAskRates = new double[noOfPriceBands + 5];
			double[] newBidRates = new double[noOfPriceBands + 5];

			final int noOfExistingAmounts = ((amounts != null) ? amounts.length : 0);
			for (int i = 0; i < newAmounts.length; i++) {
				if (i < noOfExistingAmounts) {
					newAmounts[i] = this.amounts[i];
					newAskRates[i] = this.askRates[i];
					newBidRates[i] = this.bidRates[i];
				} else {
					newAmounts[i] = PriceUpdate.NO_AMOUNT;
					newAskRates[i] = PriceUpdate.NO_PRICE;
					newBidRates[i] = PriceUpdate.NO_PRICE;
				}
			}
			this.amounts = newAmounts;
			this.askRates = newAskRates;
			this.bidRates = newBidRates;
		}
	}

	/**
	 * if we want to continue with a already started price sequence
	 * 
	 * @param startValue
	 */
	public static void initPriceIdSequence(long startValue) {
		priceIdSequence.set(startValue);
	}

	public PriceUpdate() {
		this.streamId = new EfficientStreamId();
		this.currencyCouple = new CurrencyCouple();
	}

	public PriceUpdate(int allocatedPriceBands, String mMakerName, String streamId, CurrencyCouple currencyCouple, boolean isExclusivePriceUpdate,
			boolean hasAggregateablePriceBands) {
		this.id = priceIdSequence.incrementAndGet();
		this.streamId = new EfficientStreamId(mMakerName, streamId, isExclusivePriceUpdate);

		this.currencyCouple = CurrencyCouple.newPreallocate();
		this.currencyCouple.internalize(currencyCouple);
		this.hasAggregateablePriceBands = hasAggregateablePriceBands;
		this.amounts = new double[allocatedPriceBands];
		this.askRates = new double[allocatedPriceBands];
		this.bidRates = new double[allocatedPriceBands];
	}

	public PriceUpdate(int allocatedPriceBands) {
		this(allocatedPriceBands, "", "", CurrencyCouple.newPreallocate(), NON_EXCLUSIVE, AGGREGATEABLE_BANDS);
	}

	public void addBand(double amount, double bid, double ask) {
		resizeArraysIfNecessary();
		this.amounts[noOfPriceBands] = amount;
		this.askRates[noOfPriceBands] = ask;
		this.bidRates[noOfPriceBands++] = bid;
	}

	public void addAsk(double amount, double ask) {
		resizeArraysIfNecessary();
		this.amounts[noOfPriceBands] = amount;
		this.askRates[noOfPriceBands] = ask;
		this.bidRates[noOfPriceBands++] = NO_PRICE;
	}

	public void addBid(double amount, double bid) {
		resizeArraysIfNecessary();
		this.amounts[noOfPriceBands] = amount;
		this.askRates[noOfPriceBands] = NO_PRICE;
		this.bidRates[noOfPriceBands++] = bid;
	}

	public void updateBand(int index, double bidRate, double askRate) {
		updateBid(index, bidRate);
		updateAsk(index, askRate);
	}

	public void updateBid(int index, double bidRate) {
		updateChangedPrice |= (this.bidRates[index] != bidRate);
		this.bidRates[index] = bidRate;
	}

	public void updateAsk(int index, double askRate) {
		updateChangedPrice |= (this.askRates[index] != askRate);
		this.askRates[index] = askRate;
	}

	/**
	 * prepare the existing object to act as a new quote
	 */
	public void refurbish() {
		this.id = priceIdSequence.incrementAndGet();
		for (int i = 0; i < amounts.length; i++) {
			amounts[i] = askRates[i] = bidRates[i] = NO_AMOUNT;
		}
		noOfPriceBands = 0;
	}

	public void internalize(PriceUpdate source) {
		this.id = source.id;
		this.streamId.internalize(source.streamId);
		this.transformationId = source.transformationId;
		this.hasAggregateablePriceBands = source.hasAggregateablePriceBands;
		this.noOfPriceBands = source.noOfPriceBands;
		this.currencyCouple.internalize(source.currencyCouple);
		this.updateChangedPrice = source.updateChangedPrice;

		resizeArraysIfNecessary();

		for (int i = 0; i < noOfPriceBands; i++) {
			this.amounts[i] = source.amounts[i];
			this.askRates[i] = source.askRates[i];
			this.bidRates[i] = source.bidRates[i];
		}
	}

	public void resetChangedFlag() {
		updateChangedPrice = false;
	}

	/**
	 * @return true if the price contains a change to the last update of the stream
	 */
	public boolean updateChangedPrice() {
		return updateChangedPrice;
	}

	public double bandLiquidity(int i) {
		return this.amounts[i];
	}

	public double bandAskRate(int i) {
		return this.askRates[i];
	}

	public double bandBidRate(int i) {
		return this.bidRates[i];
	}

	public int noOfPriceBands() {
		return noOfPriceBands;
	}

	public EfficientStreamId streamId() {
		return streamId;
	}

	public CurrencyCouple currencyCouple() {
		return currencyCouple;
	}

	public void setCurrencyCouple(CurrencyCouple currencyCouple) {
		this.currencyCouple = currencyCouple;
	}

	@Override
	public String toString() {
		return "PriceUpdate{" + "streamId=" + streamId + ", currencyCouple=" + currencyCouple + ", hasAggregateablePriceBands="
				+ hasAggregateablePriceBands
				+ ", id=" + id + ", transformationId=" + transformationId + ", noOfPriceBands="
				+ noOfPriceBands + ", amounts=" + Arrays.toString(amounts) + ", askRates=" + Arrays.toString(askRates) + ", bidRates="
				+ Arrays.toString(bidRates) + '}';
	}

	public boolean isEmpty() {
		return noOfPriceBands == 0;
	}
}
