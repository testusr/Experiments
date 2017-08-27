package smeo.experiments.quickfixj;

import quickfix.*;
import quickfix.field.*;
import quickfix.fix42.ExecutionReport;
import quickfix.fix44.NewOrderSingle;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

/**
 * Created by smeo on 22.02.17.
 */
public class FixClient extends MessageCracker implements Application {

    /**
     * (non-Javadoc)
     *
     * @see quickfix.Application#onCreate(quickfix.SessionID)
     */
    @Override
    public void onCreate(SessionID sessionId) {

    }

    /**
     * (non-Javadoc)
     *
     * @see quickfix.Application#onLogon(quickfix.SessionID)
     */
    @Override
    public void onLogon(SessionID sessionId) {
        System.out.println("On logged on");
    }

    /**
     * (non-Javadoc)
     *
     * @see quickfix.Application#onLogout(quickfix.SessionID)
     */
    @Override
    public void onLogout(SessionID sessionId) {

    }

    /**
     * (non-Javadoc)
     *
     * @see quickfix.Application#toAdmin(quickfix.Message, quickfix.SessionID)
     */
    @Override
    public void toAdmin(Message message, SessionID sessionId) {

    }

    /**
     * (non-Javadoc)
     *
     * @see quickfix.Application#fromAdmin(quickfix.Message, quickfix.SessionID)
     */
    @Override
    public void fromAdmin(Message message, SessionID sessionId)
            throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue,
            RejectLogon {

    }

    /**
     * (non-Javadoc)
     *
     * @see quickfix.Application#toApp(quickfix.Message, quickfix.SessionID)
     */
    @Override
    public void toApp(Message message, SessionID sessionId) throws DoNotSend {
        System.out.println("toApp: " + message);
    }

    /*** (non-Javadoc)
     * @see quickfix.Application#fromApp(quickfix.Message, quickfix.SessionID)
     */
    @Override
    public void fromApp(Message message, SessionID sessionId)
            throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue,
            UnsupportedMessageType {
        crack(message, sessionId);
    }

    @Override
    protected void onMessage(Message message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
        System.out.println("onMessage: " + message);
        if (message instanceof ExecutionReport) {
            ExecutionReport exmessage = (ExecutionReport) message;
            System.out.println("Received Execution report from server");
            System.out.println("Order Id : " + exmessage.getOrderID().getValue());
            System.out.println("Order Status : " + exmessage.getOrdStatus().getValue());
            System.out.println("Order Price : " + exmessage.getPrice().getValue());
        }
    }

    public static void main(String[] args) {
        SocketInitiator socketInitiator = null;
        try {
            InputStream is = FixServer.class.getResourceAsStream("client.cfg");
            SessionSettings initiatorSettings = new SessionSettings(is);
            Application initiatorApplication = new FixClient();
            FileStoreFactory fileStoreFactory = new FileStoreFactory(
                    initiatorSettings);
            FileLogFactory fileLogFactory = new FileLogFactory(
                    initiatorSettings);
            MessageFactory messageFactory = new DefaultMessageFactory();
            socketInitiator = new SocketInitiator(initiatorApplication, fileStoreFactory, initiatorSettings, fileLogFactory, messageFactory);
            socketInitiator.start();
            SessionID sessionId = socketInitiator.getSessions().get(0);
            Session.lookupSession(sessionId).logon();
            while (!Session.lookupSession(sessionId).isLoggedOn()) {
                System.out.println("Waiting for login success");
                Thread.sleep(1000);
            }
            System.out.println("Logged In...");

            Thread.sleep(5000);
            bookSingleOrder(sessionId);

            System.out.println("Type to quit");
            Scanner scanner = new Scanner(System.in);
            scanner.next();
            Session.lookupSession(sessionId).disconnect("Done", false);
            socketInitiator.stop();
        } catch (ConfigError e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void bookSingleOrder(SessionID sessionID) {
        System.out.println("Calling bookSingleOrder");
        //In real world this won't be a hardcoded value rather than a sequence.
        ClOrdID orderId = new ClOrdID("1");
        //to be executed on the exchange
        HandlInst instruction = new HandlInst(HandlInst.AUTOMATED_EXECUTION_ORDER_PRIVATE);
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
        newOrderSingle.set(instruction);
        newOrderSingle.set(new Price(1.0));


        try {
            Session.sendToTarget(newOrderSingle, sessionID);
        } catch (SessionNotFound e) {
            e.printStackTrace();
        }
    }
}