package smeo.experiments.chronicle.replication.echo.payload;

/**
 * Created by truehl on 10.05.17.
 */
public class PayloadBuilder {
	public static PriceUpdate bigPriceUpdate(int noOfPriceBands) {
		PriceUpdate priceUpdate = new PriceUpdate(noOfPriceBands, "LongMarketMakerName", "LongLongStreamName", new CurrencyCouple(),
				true, true);
		int baseLiquidity = 1000000;

		for (int i = 0; i < noOfPriceBands; i++) {
			if (i % 2 == 0) {
				priceUpdate.addAsk(baseLiquidity, i / 10.0);
			} else {
				priceUpdate.addAsk(baseLiquidity, i / 10.0);
				baseLiquidity *= 2;
			}
		}
		return priceUpdate;
	}
}
