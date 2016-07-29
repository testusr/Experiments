package smeo.experiments.esper.priceaggregation;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import smeo.experiments.esper.priceaggregation.domain.AuditListener;
import smeo.experiments.esper.priceaggregation.domain.PriceEventGenerator;
import smeo.experiments.esper.priceaggregation.domain.event.PriceEvent;
import smeo.experiments.esper.priceaggregation.domain.transformation.PriceTransformation;

import java.util.Random;

/**
 * Created by truehl on 22.07.16.
 */
public class Main {
	private static final String EVENT_PACKAGE_NAME = "smeo.experiments.esper.priceaggregation.domain.event";

	public static void main(String[] args) {
		Configuration config = new Configuration();

		// this allows us to not have to use package names for the events in the queries
		config.addEventTypeAutoName(EVENT_PACKAGE_NAME);

		// place to store our statements
		EPServiceProvider epService = EPServiceProviderManager.getDefaultProvider(config);

		AuditListener auditListener = null;
		// PriceTransformation askPriceTransformation = new PriceTransformation("askPrices[all]", epService, auditListener);
		// askPriceTransformation.replaceTransformation("select sum(askPrice) from PriceEvent.win:time(30 sec)");

		// PriceTransformation bidPriceTransformation = new PriceTransformation("bidPrices[all]", epService, auditListener);
		// bidPriceTransformation.replaceTransformation("select sum(bidPrice) from PriceEvent.win:time(30 sec)");

		PriceTransformation bidPriceTransformationGold = new PriceTransformation("bidPrices[GOLD]", epService, auditListener);
		bidPriceTransformationGold.replaceTransformation("select sum(bidPrice) from PriceEvent(streamName='GOLD').win:time(30 sec)");

		PriceTransformation bidPriceTransformationSilver = new PriceTransformation("bidPrices[SILVER]", epService, auditListener);
		bidPriceTransformationSilver.replaceTransformation("select avg(bidPrice) from PriceEvent(streamName='SILVER').win:time(30 sec)");

		PriceEventGenerator eventGenerator = new PriceEventGenerator();
		final double GOLD_SPREAD = 0.5;
		final double SILVER_SPREAD = 1.5;
		eventGenerator.withNoOfEvents(10000)
				.withStream("BankA", "GOLD", 10.0, GOLD_SPREAD)
				.withStream("BankB", "GOLD", 12.0, GOLD_SPREAD)
				.withStream("BankC", "GOLD", 13.0, GOLD_SPREAD)
				.withStream("BankA", "SILVER", 1.5, SILVER_SPREAD)
				.prepareEvents();

		while (true){
			eventGenerator.replayEvents(epService, 10);
		}
	}


}
