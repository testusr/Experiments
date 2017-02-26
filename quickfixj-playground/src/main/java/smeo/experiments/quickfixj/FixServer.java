package de.smeo.tnode.fix;

import quickfix.*;
import quickfix.field.*;
import quickfix.fix42.NewOrderSingle;

import java.io.InputStream;

/**
 * Created by smeo on 22.02.17.
 */
public class FixServer extends MessageCracker implements Application {
    @Override
    public void onCreate(SessionID arg0) {
        System.out.println("Successfully called onCreate for sessionId : "
                + arg0);
    }

    @Override
    public void onLogon(SessionID arg0) {
        System.out.println("Successfully logged on for sessionId : " + arg0);
    }

    @Override
    public void onLogout(SessionID arg0) {
        System.out.println("Successfully logged out for sessionId : " + arg0);
    }

    @Override
    public void toAdmin(quickfix.Message message, SessionID sessionID) {
        System.out.println("to admin: " + message);
    }

    @Override
    public void fromAdmin(quickfix.Message message, SessionID sessionID) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {
        System.out.println("Successfully called fromAdmin for sessionId : "
                + sessionID);
    }

    @Override
    public void toApp(quickfix.Message message, SessionID sessionID) throws DoNotSend {
        System.out.println("toApp: " + message);
    }

    @Override
    public void fromApp(quickfix.Message message, SessionID sessionID) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        System.out.println("Successfully called fromApp for sessionId : "
                + sessionID);
        crack(message, sessionID);

    }

    @Override
    protected void onMessage(Message message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
        if (message instanceof NewOrderSingle) {
            NewOrderSingle order = (NewOrderSingle) message;

            quickfix.fix42.ExecutionReport accept = new quickfix.fix42.ExecutionReport(
                    new OrderID("orderId"), new ExecID("execId"), new ExecTransType(ExecTransType.NEW), new ExecType(ExecType.FILL), new OrdStatus(OrdStatus.NEW), order
                    .getSymbol(), order.getSide(), new LeavesQty(0), new CumQty(0), new AvgPx(0));


            accept.set(order.getPrice());
            accept.set(order.getClOrdID());
            accept.set(order.getSymbol());
            try {
                Session.sendToTarget(accept, sessionID);
            } catch (SessionNotFound sessionNotFound) {
                sessionNotFound.printStackTrace();
            }
        } else {
            super.onMessage(message, sessionID);
        }
    }


    public static void main(String[] args) {
        String fileName = "server.cfg";
        SocketAcceptor socketAcceptor = null;
        try {

            InputStream is = FixServer.class.getResourceAsStream(fileName);
            SessionSettings executorSettings = new SessionSettings(is);
            Application application = new FixServer();
            FileStoreFactory fileStoreFactory = new FileStoreFactory(
                    executorSettings);
            DefaultMessageFactory messageFactory = new DefaultMessageFactory();
            FileLogFactory fileLogFactory = new FileLogFactory(executorSettings);
            socketAcceptor = new SocketAcceptor(application, fileStoreFactory,
                    executorSettings, fileLogFactory, messageFactory);
            socketAcceptor.start();

            SessionID sessionId = (SessionID) socketAcceptor.getSessions().get(0);
            application.onLogon(sessionId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}