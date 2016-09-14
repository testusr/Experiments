package smeo.experiments.quickfixj;

import quickfix.*;
import quickfix.field.BeginString;
import quickfix.field.HeartBtInt;
import quickfix.field.ResetSeqNumFlag;
import quickfix.fix42.Logon;

import java.io.InputStream;

/**
 * https://mprabhat.me/2012/07/02/creating-a-fix-initiator-using-quickfixj/
 */
public class TestQuickFixJConnectivity {
    public static void main(String[] args) {
        SocketInitiator socketInitiator = null;
        try {
            InputStream is = TestQuickFixJConnectivity.class.getResourceAsStream("/sessionsettings.txt");
            SessionSettings sessionSettings = new SessionSettings(is);
            Application application = new TestApplicationImpl();
            FileStoreFactory fileStoreFactory = new FileStoreFactory(sessionSettings);
            FileLogFactory logFactory = new FileLogFactory(sessionSettings);
            MessageFactory messageFactory = new DefaultMessageFactory();
            socketInitiator = new SocketInitiator(application,
                    fileStoreFactory, sessionSettings, logFactory,
                    messageFactory);
            socketInitiator.start();
            SessionID sessionId = socketInitiator.getSessions().get(0);
            sendLogonRequest(sessionId);
            int i = 0;
            do {
                try {
                    Thread.sleep(1000);
                    System.out.println(socketInitiator.isLoggedOn());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                i++;
            } while ((!socketInitiator.isLoggedOn()) && (i < 30));
        } catch (ConfigError e) {
            e.printStackTrace();
        } catch (SessionNotFound e) {
            e.printStackTrace();
        } catch (Exception exp) {
            exp.printStackTrace();
        } finally {
            if (socketInitiator != null) {
                socketInitiator.stop(true);
            }
        }
    }
    private static void sendLogonRequest(SessionID sessionId)
            throws SessionNotFound {
        Logon logon = new Logon();
        Message.Header header = logon.getHeader();
        header.setField(new BeginString("FIX.4.2"));
        logon.set(new HeartBtInt(30));
        logon.set(new ResetSeqNumFlag(true));
        boolean sent = Session.sendToTarget(logon, sessionId);
        System.out.println("Logon Message Sent : " + sent);
    }
}
