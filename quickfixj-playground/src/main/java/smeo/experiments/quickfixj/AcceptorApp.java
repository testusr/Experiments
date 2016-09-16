package smeo.experiments.quickfixj;

import quickfix.*;

/**
 * Created by smeo on 16.09.16.
 */
public class AcceptorApp implements Application {
    @Override
    public void onCreate(SessionID sessionID) {
        System.out.println("onCreate sessionId " + sessionID);
    }

    @Override
    public void onLogon(SessionID sessionID) {
        System.out.println("onLogon sessionId " + sessionID);

    }

    @Override
    public void onLogout(SessionID sessionID) {
        System.out.println("onLogout sessionId " + sessionID);

    }

    @Override
    public void toAdmin(Message message, SessionID sessionID) {
        System.out.println("toAdmin\n message: "+message+"\n-sessionId: " + sessionID);
    }

    @Override
    public void fromAdmin(Message message, SessionID sessionID) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {
        System.out.println("fromAdmin\n message: "+message+"\n-sessionId: " + sessionID);

    }

    public void toApp(Message message, SessionID sessionId) throws DoNotSend {
        System.out.println("toApp\n message : "+message+"\n , sessionID: " +sessionId);


    }

    @Override
    public void fromApp(Message message, SessionID sessionID) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        System.out.println("fromApp\n message: "+message+"\n-sessionId: " + sessionID);

    }
}
