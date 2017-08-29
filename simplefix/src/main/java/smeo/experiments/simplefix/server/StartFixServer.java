package smeo.experiments.simplefix.server;

import smeo.experiments.simplefix.model.SimpleFixMessage;
import smeo.experiments.simplefix.model.SimpleFixMessageParser;

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

		String originalFixMessage = "8=FIX.4.4\u00019=857\u000135=W\u000134=1559\u000149=Server.CompID\u000150=Server.SubID\u000152=20170829-13:55:14.262\u000156=Client.CompID\u000157=Client.SubID\u000155=AED/AFN\u0001262=AED/AFN\u00017075=1504014914261\u00017076=1799416063006133\u00017077=1799416063008655\u00017078=1799416063097441\u00017079=1796016\u000120000=474957379\u000120001=1799416062997653\u0001268=12\u0001269=0\u0001270=1.2\u0001271=1000000\u00011070=1\u0001278=1796016/0/\u0001269=0\u0001270=1.1\u0001271=2000000\u00011070=1\u0001278=1796016/1/\u0001269=0\u0001270=1\u0001271=5000000\u00011070=1\u0001278=1796016/2/\u0001269=0\u0001270=0.9\u0001271=10000000\u00011070=1\u0001278=1796016/3/\u0001269=0\u0001270=0.8\u0001271=20000000\u00011070=1\u0001278=1796016/4/\u0001269=0\u0001270=0.7\u0001271=50000000\u00011070=1\u0001278=1796016/5/\u0001269=1\u0001270=1.4\u0001271=1000000\u00011070=1\u0001278=1796016/20/\u0001269=1\u0001270=1.5\u0001271=2000000\u00011070=1\u0001278=1796016/21/\u0001269=1\u0001270=1.6\u0001271=5000000\u00011070=1\u0001278=1796016/22/\u0001269=1\u0001270=1.7\u0001271=10000000\u00011070=1\u0001278=1796016/23/\u0001269=1\u0001270=1.8\u0001271=20000000\u00011070=1\u0001278=1796016/24/\u0001269=1\u0001270=1.9\u0001271=50000000\u00011070=1\u0001278=1796016/25/\u000110=004\u0001";
		SimpleFixMessageParser.parseFromFixString(originalFixMessage, "\u0001", fixMessage);

		for (int i = 0; i < 20; i++) {
			if (clientSession.isConnected()) {
				clientSession.sendMessage(fixMessage);
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		while (true) {
			simpleFixServer.readMessages();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

}
