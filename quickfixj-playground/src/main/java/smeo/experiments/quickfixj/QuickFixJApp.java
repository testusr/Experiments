package smeo.experiments.quickfixj;

import quickfix.*;
import quickfix.field.*;
import quickfix.fix42.NewOrderSingle;

import java.util.Date;

/**
 * Created by smeo on 13.09.16.
 */
public class QuickFixJApp implements Application {
    @Override
    public void onCreate(SessionID sessionID) {
        System.out.println("on create sessionId: " + sessionID);
    }

    @Override
    public void onLogon(SessionID sessionID) {
        System.out.println("on logon sessionID: " + sessionID);
        executeOrder(sessionID);
    }

    private void executeOrder(SessionID sessionID) {
        System.out.println("sending order message.......");
        NewOrderSingle order = new NewOrderSingle(new ClOrdID("MISYS1001"),
                new HandlInst(HandlInst.MANUAL_ORDER), new Symbol("MISYS"), new Side(Side.BUY), new TransactTime(new Date()), new OrdType(OrdType.LIMIT));
        try {
            Session.sendToTarget(order, sessionID);
        } catch (SessionNotFound sessionNotFound) {
            sessionNotFound.printStackTrace();
        }
    }

    @Override
    public void onLogout(SessionID sessionID) {
        System.out.println("onLogout sessionID: " + sessionID);
    }

    @Override
    public void toAdmin(Message message, SessionID sessionID) {
        System.out.println("toAdmin\n message : "+message+"\n , sessionID: " +sessionID);

    }

    @Override
    public void fromAdmin(Message message, SessionID sessionID) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {
        System.out.println("fromAdmin\n message : "+message+"\n , sessionID: " +sessionID);

    }

    @Override
    public void toApp(Message message, SessionID sessionID) throws DoNotSend {
        System.out.println("toApp\n message : "+message+"\n , sessionID: " +sessionID);

    }

    @Override
    public void fromApp(Message message, SessionID sessionID) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        System.out.println("fromAdmin\n message : "+message+"\n , sessionID: " +sessionID);

    }
}