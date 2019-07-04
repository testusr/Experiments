package smeo.experiments.simplefix.testclient;

import quickfix.*;
import quickfix.fix50.ExecutionReport;
import quickfix.fix50.MarketDataSnapshotFullRefresh;
import quickfix.fix50.MessageCracker;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

/**
 * Created by smeo on 22.02.17.
 */
public class SimpleQuickFixClient extends MessageCracker implements Application {
    private final int clientId;
    int toApp = 0;

    public SimpleQuickFixClient(int clientId) {
        this.clientId = clientId;
    }

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
    public void fromAdmin(Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {

    }

    /**
     * (non-Javadoc)
     *
     * @see quickfix.Application#toApp(quickfix.Message, quickfix.SessionID)
     */
    @Override
    public void toApp(Message message, SessionID sessionId) throws DoNotSend {
        System.out.println("[CLIENT " + clientId + "] toApp (" + ++toApp + "): " + message);
    }

    /*** (non-Javadoc)
     * @see quickfix.Application#fromApp(quickfix.Message, quickfix.SessionID)
     */
    @Override
    public void fromApp(Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        crack(message, sessionId);
    }

    @Override
    public void onMessage(MarketDataSnapshotFullRefresh message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
        System.out.println("[CLIENT " + clientId + "] fullRefresh: " + message);
    }

    @Override
    public void onMessage(Message message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
        System.out.println("[CLIENT " + clientId + "] onMessage: " + message);
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


    public static void main(String[] args) {
        SocketInitiator socketInitiator = null;
        try {
            InputStream is = SimpleQuickFixClient.class.getResourceAsStream("/client.cfg");
            SessionSettings initiatorSettings = new SessionSettings(is);
            Application initiatorApplication = new SimpleQuickFixClient(0);
            FileStoreFactory fileStoreFactory = new FileStoreFactory(initiatorSettings);
            FileLogFactory fileLogFactory = new FileLogFactory(initiatorSettings);
            SLF4JLogFactory logFactory = new SLF4JLogFactory(initiatorSettings);
            MessageFactory messageFactory = new DefaultMessageFactory();
            socketInitiator = new SocketInitiator(initiatorApplication, fileStoreFactory, initiatorSettings, logFactory, messageFactory);
            socketInitiator.start();
            SessionID sessionId = socketInitiator.getSessions()
                    .get(0);
            Session.lookupSession(sessionId)
                    .logon();
            while (!Session.lookupSession(sessionId)
                    .isLoggedOn()) {
                System.out.println("Waiting for login success");
                Thread.sleep(1000);
            }
            System.out.println("Logged In...");

            Thread.sleep(5000);

            System.out.println("Type to quit");
            Scanner scanner = new Scanner(System.in);
            scanner.next();
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

}