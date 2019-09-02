package smeo.experiments.esper.priceaggregation.domain;

import com.espertech.esper.client.EPServiceProvider;
import smeo.experiments.esper.priceaggregation.domain.event.PriceEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by smeo on 23.07.16.
 */
public class PriceEventGenerator {
	private static AtomicLong idSequence = new AtomicLong(0);
	private int noOfEvents = 10000;
	private boolean randomizeSpread = false;
	private List<Stream> streams = new ArrayList<Stream>();

	private PriceEvent[] preallocatedPrices;

	/**
	 * default 10000
	 * 
	 * @param noOfEvents
	 * @return
	 */
	public PriceEventGenerator withNoOfEvents(int noOfEvents) {
		this.noOfEvents = noOfEvents;
		return this;
	}

	public PriceEventGenerator withStream(String bankName, String streamName, double baseValue, double maxSpread) {
		this.streams.add(new Stream(bankName, streamName, baseValue, maxSpread));
		return this;
	}

	public PriceEventGenerator doRandomizeSpread() {
		this.randomizeSpread = true;
		return this;
	}

	/**
	 * preallocate events for later replay
	 */
	public void prepareEvents() {
		this.preallocatedPrices = new PriceEvent[noOfEvents];
		Random random = new Random(System.currentTimeMillis());

		for (int i = 0; i < noOfEvents; i++) {
			Stream stream = streams.get(i % streams.size());
			if (randomizeSpread) {
				preallocatedPrices[i] = createRandomPrice(stream, random);
			} else {
				preallocatedPrices[i] = createPrice(stream);
			}
		}
	}

	private PriceEvent createPrice(Stream stream) {
		final double bidAskSpread = stream.maxSpread / 2.0;
		double bidPrice = stream.baseValue + bidAskSpread;
		double askPrice = stream.baseValue - bidAskSpread;

		PriceEvent price = new PriceEvent(idSequence.incrementAndGet(),
				stream.streamName(),
				stream.bankName(),
				bidPrice,
				askPrice);

		return price;
	}

	public void replayEvents(EPServiceProvider epService, long delay) {
		for (int i = 0; i < this.preallocatedPrices.length; i++) {
			epService.getEPRuntime().sendEvent(preallocatedPrices[i]);
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private PriceEvent createRandomPrice(Stream stream, Random random) {
		final double randomPercentage = random.nextInt(100) / 100.0;

		final int randomValue = random.nextInt((int) (stream.maxSpread() * 1000));
		double totalSpread = randomValue / 1000.0;
		double askSpread = totalSpread * randomPercentage;
		double bidSpread = totalSpread - askSpread;

		double bidPrice = stream.baseValue + askSpread;
		double askPrice = stream.baseValue - bidSpread;

		PriceEvent price = new PriceEvent(idSequence.incrementAndGet(),
				stream.streamName(),
				stream.bankName(),
				bidPrice,
				askPrice);

		return price;
	}

	private static class Stream {
		private final String bankName;
		private final String streamName;
		private final double baseValue;
		private final double maxSpread;

		public Stream(String bankName, String streamName, double baseValue, double maxSpread) {
			this.bankName = bankName;
			this.streamName = streamName;
			this.baseValue = baseValue;
			this.maxSpread = maxSpread;
		}

		public String bankName() {
			return bankName;
		}

		public String streamName() {
			return streamName;
		}

		public double baseValue() {
			return baseValue;
		}

		public double maxSpread() {
			return maxSpread;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;

			Stream stream = (Stream) o;

			if (Double.compare(stream.baseValue, baseValue) != 0)
				return false;
			if (Double.compare(stream.maxSpread, maxSpread) != 0)
				return false;
			if (bankName != null ? !bankName.equals(stream.bankName) : stream.bankName != null)
				return false;
			return streamName != null ? streamName.equals(stream.streamName) : stream.streamName == null;

		}

		@Override
		public int hashCode() {
			int result;
			long temp;
			result = bankName != null ? bankName.hashCode() : 0;
			result = 31 * result + (streamName != null ? streamName.hashCode() : 0);
			temp = Double.doubleToLongBits(baseValue);
			result = 31 * result + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(maxSpread);
			result = 31 * result + (int) (temp ^ (temp >>> 32));
			return result;
		}

	}

}
