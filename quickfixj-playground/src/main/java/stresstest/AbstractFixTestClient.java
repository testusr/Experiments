package stresstest;

import quickfix.*;
import quickfix.field.*;
import quickfix.fix42.ExecutionReport;
import quickfix.fix44.Logon;
import quickfix.fix44.NewOrderSingle;

import java.io.*;
import java.util.Properties;

public abstract class AbstractFixTestClient extends MessageCracker implements Application {
	volatile Boolean loginSuccesfull;

	@Override
	public void onCreate(SessionID sessionId) {}
	@Override
	public void onLogon(SessionID sessionId) {
		System.out.println("On logged on");
	}
	@Override
	public void onLogout(SessionID sessionId) {}
	@Override
	public void toAdmin(Message message, SessionID sessionId) {}
	@Override
	public void fromAdmin(Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {}
	@Override
	public void toApp(Message message, SessionID sessionId) throws DoNotSend {System.out.println("toApp: " + message); }

	public void fromApp(Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
		crack(message, sessionId);
	}

	@Handler
	public void handleLogon(Logon logonMessage, SessionID sessionID){
		loginSuccesfull = Boolean.TRUE;
	}

	@Handler
	public void handle(RejectLogon rejectLogon, SessionID sessionID){
		loginSuccesfull = Boolean.FALSE;
	}

	@Override
	protected void onMessage(Message message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
		System.out.println("onMessage: " + message);
		if (message instanceof ExecutionReport) {
			ExecutionReport exmessage = (ExecutionReport) message;
			System.out.println("Received Execution report from server");
			System.out.println("Order Id : " + exmessage.getOrderID()
					.getValue());
			System.out.println("Order Status : " + exmessage.getOrdStatus()
					.getValue());
			System.out.println("Order Price : " + exmessage.getPrice()
					.getValue());
		}
	}


	public void start(String[] args) {
		SocketInitiator socketInitiator = null;
		try {
			String fixconfig = args[0];

			InputStream is = AbstractFixTestClient.class.getResourceAsStream("client.cfg");
			SessionSettings initiatorSettings = new SessionSettings(is);
			Application initiatorApplication = this;
			FileStoreFactory fileStoreFactory = new FileStoreFactory(initiatorSettings);
			FileLogFactory fileLogFactory = new FileLogFactory(initiatorSettings);
			SLF4JLogFactory logFactory = new SLF4JLogFactory(initiatorSettings);
			MessageFactory messageFactory = new DefaultMessageFactory();
			socketInitiator = new SocketInitiator(initiatorApplication, fileStoreFactory, initiatorSettings, logFactory, messageFactory);
			socketInitiator.start();
			SessionID sessionId = socketInitiator.getSessions().get(0);
			Session.lookupSession(sessionId)
					.logon();

			while (!Session.lookupSession(sessionId)
					.isLoggedOn()) {
				System.out.println("Waiting for login success");
				Thread.sleep(1000);
			}
			System.out.println("Logged In... starting test");
			runtest(loadproperties(args[1]));
			System.out.println("test finished");
			Thread.sleep(2000);
			System.out.println("disconnecting session");
			Session.lookupSession(sessionId)
					.disconnect("Done", false);
			socketInitiator.stop();
		} catch (ConfigError e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	abstract void runtest(Properties testproperties);

	public boolean login(String username, String password, int timeoutInMs){
		int iterations = timeoutInMs / 100;
		for (int i=0; i < iterations; i++){
			if (loginSuccesfull != null){
				return loginSuccesfull;
			}
		}
		throw new IllegalArgumentException("TIMEOUT while waiting for login");
	}



	private static Properties loadproperties(String propertyfile) {
		Properties prop = new Properties();
		if (!propertyfile.trim().isEmpty()) {
			try (InputStream input = new FileInputStream(propertyfile)) {

				// load a properties file
				prop.load(input);

			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return prop;
	}

	private static void bookSingleOrder(SessionID sessionID) {
		System.out.println("Calling bookSingleOrder");
		//In real world this won't be a hardcoded value rather than a sequence.
		ClOrdID orderId = new ClOrdID("1");
		//to be executed on the exchange
		//Since its FX currency pair name
		Symbol mainCurrency = new Symbol("EUR/USD");
		//Which side buy, sell
		Side side = new Side(Side.BUY);
		//Time of transaction
		TransactTime transactionTime = new TransactTime();
		//Type of our order, here we are assuming this is being executed on the exchange
		OrdType orderType = new OrdType(OrdType.FOREX_MARKET);
		NewOrderSingle newOrderSingle = new NewOrderSingle(orderId, side, transactionTime, orderType);
		//Quantity
		newOrderSingle.set(new OrderQty(100));
		newOrderSingle.set(mainCurrency);
		newOrderSingle.set(new Price(1.0));
		newOrderSingle.set(new HandlInst('1'));


		try {
			Session.sendToTarget(newOrderSingle, sessionID);
		} catch (SessionNotFound e) {
			e.printStackTrace();
		}
	}
}