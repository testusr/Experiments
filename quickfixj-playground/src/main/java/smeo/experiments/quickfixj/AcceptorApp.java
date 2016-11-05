package smeo.experiments.quickfixj;

import quickfix.*;
import quickfix.field.QuoteReqID;
import quickfix.field.Symbol;
import quickfix.fix42.MarketDataSnapshotFullRefresh;
import quickfix.fix42.Quote;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by smeo on 16.09.16.
 */
public class AcceptorApp implements Application {
    Map<SessionID, QuoteSender> quoteSenders = new HashMap<>();
    @Override
    public void onCreate(SessionID sessionID) {
        System.out.println("onCreate sessionId " + sessionID);
    }

    @Override
    public void onLogon(SessionID sessionID) {
        System.out.println("onLogon sessionId " + sessionID);
      //  startSendingQuotesToSession(sessionID);

    }

    private void startSendingQuotesToSession(SessionID sessionID) {
        if (quoteSenders.containsKey(sessionID)){
            throw new IllegalArgumentException("session already started");
        }
        QuoteSender value = new QuoteSender(sessionID);
        value.start();
        quoteSenders.put(sessionID, value);
    }

    @Override
    public void onLogout(SessionID sessionID) {
        System.out.println("onLogout sessionId " + sessionID);
        stopSendingQuotesToSession(sessionID);
    }

    private void stopSendingQuotesToSession(SessionID sessionID) {
        QuoteSender quoteSender = quoteSenders.get(sessionID);
        if (quoteSender == null){
            throw new IllegalArgumentException("no session found with id '"+sessionID+"' ");
        }
        quoteSender.stopSending();

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

    private class QuoteSender extends Thread {
        private SessionID sessionID;
        private boolean isRunning = true;

        public QuoteSender(SessionID sessionID) {
            this.sessionID = sessionID;
        }

        @Override
        public void run() {

            while (isRunning){
            Quote test = getQuote();
            try {
                System.out.println(Session.sendToTarget(test, sessionID) ?
                                "send '"+sessionID+"'" : "not send '"+sessionID+"'");
            } catch (SessionNotFound sessionNotFound) {
                sessionNotFound.printStackTrace();
            }
            }
            System.out.println("stopped sending quotes to session '"+sessionID+"'");

        }

        public void stopSending() {
            this.isRunning = false;
        }
    }

    private Quote getQuote() {
        MarketDataSnapshotFullRefresh fullRefresh;

        Quote quote = new Quote();
        quote.set(new QuoteReqID("QRI-"+SystemTime.currentTimeMillis()));
        quote.set(new Symbol("EUR/USD"));

        return quote;
    }
}
