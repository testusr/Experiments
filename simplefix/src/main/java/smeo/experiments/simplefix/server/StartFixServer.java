package smeo.experiments.simplefix.server;

import smeo.experiments.simplefix.model.FixTags;
import smeo.experiments.simplefix.model.SimpleFixMessage;

/**
 * Created by truehl on 15.08.17.
 */
public class StartFixServer {
	public static final int LISTEN_TO_PORT = 5001;

	public static final int MDEntry_Type_BID = 0;
	public static final int MDEntry_Type_OFFER = 1;
	public static final int MdQuoteType_Tradable = 1;

	public static void main(String[] args) {
		SimpleFixServer simpleFixServer = new SimpleFixServer(LISTEN_TO_PORT, "localhost");

		SimpleSessionConfig clientSessionConfig = SimpleSessionConfig.builder()
				.beginString("FIX.4.2")
				.targetCompID("Server.CompID")
				.targetSubID("Server.SubId")
				.senderCompID("Client.CompID")
				.senderSubID("ClientSubId")
				.build();

		SimpleFixSession clientSession = simpleFixServer.setupSession(clientSessionConfig);
		simpleFixServer.startListening();

		SimpleFixMessage fixMessage = new SimpleFixMessage();

		while (!clientSession.isConnected()) {
			System.out.println("waiting for session to connect");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("start sending snapshots");
		for (int i = 0; i < 3; i++) {
			if (clientSession.isConnected()) {
				fixMessage.refurbish();
				fixMessage.beginString("FIX4.2");
				fixMessage.messageType("W");
				fixMessage.addTag(FixTags.Symbol.tag, "EUR/GBP");
				fixMessage.addTag(FixTags.MDReqId.tag, "EUR/GBP");
				fixMessage.addTag(FixTags.NoMDEntries.tag, 2);
				// MDEntry #1
				fixMessage.addTag(FixTags.MDEntryID.tag, "6259");
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

				clientSession.sendMessage(fixMessage);

			} else {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

	}

}
