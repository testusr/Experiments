package smeo.smeo.experiments.esper;

import com.espertech.esper.client.*;
import com.espertech.esper.event.map.MapEventBean;
import org.apache.commons.lang3.Validate;
import org.apache.log4j.Logger;
import org.junit.Assert;
import smeo.experiments.esper.event.Quote;
import smeo.experiments.esper.event.QuoteBuilder;
import smeo.experiments.utils.logging.XmlUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by truehl on 25.07.16.
 */
public class EsperTestFixture implements UpdateListener {
	private static Logger LOGGER = Logger.getLogger(EsperTestFixture.class);

	private static final String EVENT_PACKAGE_NAME = "smeo.experiments.esper.event";
	private final EPServiceProvider epService;
	private final List<Quote> outgoingQuotes = new ArrayList<Quote>();

	private EPStatement lastPriceWindow;
	private EPStatement lastPriceWindowUpdate;
	private EPStatement statement;
	private String epl;

	public EsperTestFixture() {
		Configuration config = new Configuration();
		// this allows us to not have to use package names for the events in the queries
		config.addEventTypeAutoName(EVENT_PACKAGE_NAME);
		// place to store our statements
		this.epService = EPServiceProviderManager.getDefaultProvider(config);
		setupLastPriceWindow();

	}

	/**
	 * Setting up a window accessible from several statements containing the last price of a bank
	 */
	private void setupLastPriceWindow() {
		final EPAdministrator epAdministrator = epService.getEPAdministrator();

		lastPriceWindow = epAdministrator.createEPL("create window lastPriceWindow.win:keepall() as Quote");
		lastPriceWindowUpdate = epAdministrator.createEPL(
				"@Audit " +
						"on Quote qe " +
						"  merge lastPriceWindow lpWin " +
						"  where lpWin.bank = qe.bank " +
						"  when matched " +
						"    then update set lpWin.streamName = qe.streamName, lpWin.bidRate = qe.bidRate, lpWin.askRate = qe.askRate" +
						"  when not matched" +
						"    then insert select *");
	}

	public void loadEPL(String eplExpression) {
		Validate.isTrue(statement == null, "EPL statment was already loaded, cannot be overwritten");
		final EPAdministrator epAdministrator = epService.getEPAdministrator();

		this.statement = epAdministrator.createEPL(eplExpression);
		this.epl = eplExpression;
		this.statement.addListener(this);
	}

	public void replayQuotes(List<Quote> incomingQuoteList) {
		Validate.isTrue(statement != null, "There is currently no EPL statement loaded, pls load one first");

		for (int i = 0; i < incomingQuoteList.size(); i++) {
			epService.getEPRuntime().sendEvent(incomingQuoteList.get(i));
		}
	}

	public void assertResultSetSize(int expectedSize) {
		Assert.assertEquals("outgoingQuotes list did not match expected size", expectedSize, outgoingQuotes.size());
	}

	public List<Quote> outgoingQuotes() {
		return outgoingQuotes;
	}

	public void update(EventBean[] newEvents, EventBean[] oldEvents) {
		if (newEvents.length > 1) {
			System.out.println("reveived event count > 1 => " + newEvents.length);
			LOGGER.warn("reveived event count > 1 => " + newEvents.length);
		}
		EventBean event = newEvents[0];

		if (event instanceof MapEventBean) {
			printMappedEvent((MapEventBean) event);
			outgoingQuotes.add(quoteFromEvent((MapEventBean) event));
		} else {
			printEvent(event);
		}
	}

	private void printEvent(EventBean event) {
		System.out.println(XmlUtils.objectToXml(event));
	}

	private Quote quoteFromEvent(MapEventBean mapEventBean) {
		QuoteBuilder quoteBuilder = new QuoteBuilder((String) mapEventBean.get("bank"));

		return quoteBuilder.create();
	}

	private void printMappedEvent(MapEventBean event) {
		final MapEventBean mappedEvent = event;

		final Iterator<Map.Entry<String, Object>> iProperties = mappedEvent.getProperties()
				.entrySet()
				.iterator();

		StringBuilder eventValues = new StringBuilder();
		eventValues.append("Event[");
		while (iProperties.hasNext()) {
			final Map.Entry<String, Object> next = iProperties.next();
			eventValues.append(next.getKey()).append("= ").append(next.getValue()).append("\n");
		}
		eventValues.append("]:\n");
		eventValues.append("--\n");
		System.out.println(eventValues.toString());
	}
}
