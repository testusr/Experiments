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
		fixMessage.addTag(FixTags.MDEntryID.tag, "6260");
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

}