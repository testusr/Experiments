package smeo.experiments.esper.priceaggregation;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import smeo.experiments.esper.priceaggregation.domain.AuditListener;
import smeo.experiments.esper.priceaggregation.domain.transformation.PriceTransformation;
import smeo.experiments.esper.priceaggregation.domain.event.Price;

import java.util.Random;

/**
 * Created by truehl on 22.07.16.
 */
public class Main {
	private static final String[] streamNames = { "INDICATIVE", "BRONCE", "SILVER", "GOLD" };
	private static final String[] bankNames = { "BankA", "BankB", "BankC", "BankD", "BankE", "BankF" };

	public static void main(String[] args) {
		Configuration config = new Configuration();
		config.addEventTypeAutoName("smeo.experiments.esper.priceaggregation.domain.event");

		EPServiceProvider epService = EPServiceProviderManager.getDefaultProvider(config);

		AuditListener auditListener = null;
		// PriceTransformation askPriceTransformation = new PriceTransformation("askPrices[all]", epService, auditListener);
		// askPriceTransformation.replaceTransformation("select sum(askPrice) from Price.win:time(30 sec)");

		// PriceTransformation bidPriceTransformation = new PriceTransformation("bidPrices[all]", epService, auditListener);
		// bidPriceTransformation.replaceTransformation("select sum(bidPrice) from Price.win:time(30 sec)");

		PriceTransformation bidPriceTransformationGold = new PriceTransformation("bidPrices[GOLD]", epService, auditListener);
		bidPriceTransformationGold.replaceTransformation("select sum(bidPrice) from Price(streamName='GOLD').win:time(30 sec)");

		PriceTransformation bidPriceTransformationSilver = new PriceTransformation("bidPrices[SILVER]", epService, auditListener);
		bidPriceTransformationSilver.replaceTransformation("select avg(bidPrice) from Price(streamName='SILVER').win:time(30 sec)");

		generateEvents(epService);
	}

	private static void generateEvents(EPServiceProvider epService) {
		Random random = new Random(System.currentTimeMillis());
		Price[] preallocatedPrices = new Price[10000];
		for (int i = 0; i < preallocatedPrices.length; i++) {
			preallocatedPrices[i] = createPrice(i);
		}
		int i = 0;
		while (true) {
			i++;
			final int index = i % preallocatedPrices.length;
			epService.getEPRuntime().sendEvent(preallocatedPrices[index]);

			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private static Price createPrice(int i) {
		final int bankNameIndex = i % bankNames.length;
		final int streamNameIndex = i % streamNames.length;
		Price price = new Price(System.nanoTime(), streamNames[streamNameIndex], bankNames[bankNameIndex], (i + 2) * streamNameIndex, (i + 1) * streamNameIndex);
		return price;
	}
}
