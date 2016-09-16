package smeo.experiments.quickfixj;

import quickfix.*;

import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Created by smeo on 16.09.16.
 */
public class Initiator {
    public static void main(String[] args) {
        SocketInitiator socketInitiator = null;
        String fileName = "/conf/initiator.cfg";
        try {
            InputStream is = Initiator.class.getResourceAsStream(fileName);
            SessionSettings initiatorSettings = new SessionSettings(is);
            Application initiatorApplication = new InitiatorApp();
            FileStoreFactory fileStoreFactory = new FileStoreFactory(
                    initiatorSettings);
            FileLogFactory fileLogFactory = new FileLogFactory(
                    initiatorSettings);
            MessageFactory messageFactory = new DefaultMessageFactory();
            socketInitiator = new SocketInitiator(initiatorApplication, fileStoreFactory, initiatorSettings, fileLogFactory, messageFactory);
            socketInitiator.start();
            Message msg = new Message();
            msg.setString(1, "Hello this is test Message");


            SessionID sessionId = (SessionID) socketInitiator.getSessions().get(0);
            Session.lookupSession(sessionId).logon();
            initiatorApplication.onLogon(sessionId);
            initiatorApplication.toApp(msg, sessionId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
