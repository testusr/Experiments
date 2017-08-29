package smeo.experiments.simplefix.model;

import org.junit.Test;

import java.nio.ByteBuffer;

public class SimpleFixMessageTest {

	public static final int MDEntry_Type_BID = 0;
	public static final int MDEntry_Type_OFFER = 1;
	public static final int MdQuoteType_Tradable = 1;

	@Test
	public void snapshotFullRefresh() {
		SimpleFixMessage fixMessage = new SimpleFixMessage();

		fixMessage.beginString("FIX4.4");
		fixMessage.messageType("W");
		fixMessage.addTag(FixTags.Symbol.tag, "EUR/GBP");
		fixMessage.addTag(FixTags.MDReqId.tag, "EUR/GBP");
		fixMessage.addTag(FixTags.NoMDEntries.tag, 2);
		// MDEntry #1
		fixMessage.addTag(FixTags.MDEntryType.tag, MDEntry_Type_BID);
		fixMessage.addTag(FixTags.MDEntryPx.tag, 0.90958f);
		fixMessage.addTag(FixTags.MDEntrySize.tag, 1000000);
		fixMessage.addTag(FixTags.MDEntryDate.tag, 20170814);
		fixMessage.addTag(FixTags.MDEntryTime.tag, "11:30:57.066");
		fixMessage.addTag(FixTags.MdQuoteType.tag, MdQuoteType_Tradable);

		// MDEntry #2
		fixMessage.addTag(FixTags.MDEntryType.tag, MDEntry_Type_OFFER);
		fixMessage.addTag(FixTags.MDEntryPx.tag, 0.90967f);
		fixMessage.addTag(FixTags.MDEntrySize.tag, 1000000);
		fixMessage.addTag(FixTags.MDEntryDate.tag, 20170814f);
		fixMessage.addTag(FixTags.MDEntryTime.tag, "11:30:57.066");
		fixMessage.addTag(FixTags.MdQuoteType.tag, MdQuoteType_Tradable);

		System.out.println(SimpleFixMessage.asString(fixMessage));

		final ByteBuffer byteBuffer = ByteBuffer.allocate(2098);
		fixMessage.writeToByteBuffer(byteBuffer);
		SimpleFixMessage newFixMessage = new SimpleFixMessage();
		byteBuffer.flip();
		SimpleFixMessageParser parser = new SimpleFixMessageParser();
		parser.parseNextMessage(byteBuffer, newFixMessage);
		System.out.println(SimpleFixMessage.asString(fixMessage));

	}

	@Test
	public void readMessageFromString() {
		SimpleFixMessage simpleFixMessage = new SimpleFixMessage();
		String originalFixMessage = "8=FIX.4.4^A9=1332^A35=W^A34=6^A49=Server.CompID^A50=Server.SubID^A52=20170829-12:02:01.987^A56=Client.CompID^A57=Client.SubID^A55=AED/AFN^A262=AED/AFN^A7075=1504008121983^A7076=1792623784883815^A7077=1792623784886968^A7078=1792623788503463^A7079=55585^A20000=474957379^A20001=1792623784874780^A268=12^A269=0^A270=1.1^A271=1000000^A272=20170829^A273=12:02:01.983^A64=20170905^A1070=1^A278=55585/0/^A269=0^A270=1^A271=2000000^A272=20170829^A273=12:02:01.983^A64=20170905^A1070=1^A278=55585/1/^A269=0^A270=0.9^A271=5000000^A272=20170829^A273=12:02:01.983^A64=20170905^A1070=1^A278=55585/2/^A269=0^A270=0.8^A271=10000000^A272=20170829^A273=12:02:01.983^A64=20170905^A1070=1^A278=55585/3/^A269=0^A270=0.7^A271=20000000^A272=20170829^A273=12:02:01.983^A64=20170905^A1070=1^A278=55585/4/^A269=0^A270=0.6^A271=50000000^A272=20170829^A273=12:02:01.983^A64=20170905^A1070=1^A278=55585/5/^A269=1^A270=1.4^A271=1000000^A272=20170829^A273=12:02:01.983^A64=20170905^A1070=1^A278=55585/20/^A269=1^A270=1.5^A271=2000000^A272=20170829^A273=12:02:01.983^A64=20170905^A1070=1^A278=55585/21/^A269=1^A270=1.6^A271=5000000^A272=20170829^A273=12:02:01.983^A64=20170905^A1070=1^A278=55585/22/^A269=1^A270=1.7^A271=10000000^A272=20170829^A273=12:02:01.983^A64=20170905^A1070=1^A278=55585/23/^A269=1^A270=1.8^A271=20000000^A272=20170829^A273=12:02:01.983^A64=20170905^A1070=1^A278=55585/24/^A269=1^A270=1.9^A271=50000000^A272=20170829^A273=12:02:01.983^A64=20170905^A1070=1^A278=55585/25/^A10=154^A";

		SimpleFixMessageParser.parseFromFixString(originalFixMessage, "\\^A", simpleFixMessage);
		System.out.println(originalFixMessage);
		System.out.println("\n" + SimpleFixMessage.asString(simpleFixMessage));
	}

}