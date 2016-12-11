package smeo.smeo.experiments.esper;

import org.junit.Ignore;
import smeo.experiments.esper.event.Quote;
import smeo.experiments.esper.event.QuoteBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by truehl on 25.07.16.
 */
@Ignore
public class TestBestPriceSelectionViaEsper {
	private List<Quote> incomingQuoteList = new ArrayList<Quote>();
	private EsperTestFixture esperTestFixture = new EsperTestFixture();

	@org.junit.Test
	public void testBestBidPrice() {

		incomingQuoteList.add(quoteBuilder("BankA").bidRate(1.0).askRate(1.0).create());
		incomingQuoteList.add(quoteBuilder("BankA").bidRate(1.1).askRate(1.1).create());
		incomingQuoteList.add(quoteBuilder("BankB").bidRate(2.1).askRate(2.1).create());
		incomingQuoteList.add(quoteBuilder("BankC").bidRate(3.1).askRate(3.1).create());
		incomingQuoteList.add(quoteBuilder("BankC").bidRate(1.0).askRate(3.1).create());

		esperTestFixture.loadEPL("select * " +
				"from lastPriceWindow " +
				"group by bank " +
				"order by bidRate desc ");

		// esperTestFixture.loadEPL("select sum(bidQuote) from Quote(bank='BankA').win:time(30 sec)");
		esperTestFixture.replayQuotes(incomingQuoteList);

		esperTestFixture.assertResultSetSize(4);
		esperTestFixture.outgoingQuotes();
	}

	private QuoteBuilder quoteBuilder(String bankName) {
		return new QuoteBuilder(bankName);
	}

	// public void testVWAPPrice() {
	// incomingQuoteList.add(quoteBuilder("BankA").bidPrice(1.0).askPrice(1.0).liquidity(1500.0).create());
	// incomingQuoteList.add(quoteBuilder("BankA").bidPrice(1.1).askPrice(1.1).liquidity(1500.0).create());
	// incomingQuoteList.add(quoteBuilder("BankB").bidPrice(2.1).askPrice(2.1).liquidity(1500.0).create());
	// incomingQuoteList.add(quoteBuilder("BankC").bidPrice(3.1).askPrice(3.1).liquidity(1500.0).create());
	//
	// esperTestFixture.loadEPL("select * ..");
	// esperTestFixture.replayQuotes(incomingQuoteList);
	//
	// esperTestFixture.assertResultSetSize(4);
	// esperTestFixture.outgoingQuotes();
	// }
	//
	// public void testBankAndStreamSelection() {
	// incomingQuoteList.add(quoteBuilder("BankA").bidPrice(1.0).withStreamName("GOLD").create());
	// incomingQuoteList.add(quoteBuilder("BankA").bidPrice(1.1).withStreamName("SILVER").create());
	// incomingQuoteList.add(quoteBuilder("BankB").bidPrice(1.0).withStreamName("GOLD").create());
	// incomingQuoteList.add(quoteBuilder("BankC").bidPrice(1.0).withStreamName("GOLD").create());
	//
	// esperTestFixture.loadEPL("select * ..");
	// esperTestFixture.replayQuotes(incomingQuoteList);
	//
	// esperTestFixture.assertResultSetSize(4);
	// esperTestFixture.outgoingQuotes();
	// }

}
