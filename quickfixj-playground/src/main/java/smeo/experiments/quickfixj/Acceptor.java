package smeo.experiments.quickfixj;

import quickfix.*;
import quickfix.fix42.MessageFactory;

import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Created by smeo on 16.09.16.
 */
public class Acceptor {
    public static void main(String[] args) {
        String fileName = "/conf/acceptor.cfg";
        SocketAcceptor socketAcceptor = null;
        try {

            InputStream is = Acceptor.class.getResourceAsStream(fileName);
            SessionSettings executorSettings = new SessionSettings(is);
            Application application = new AcceptorApp();
            FileStoreFactory fileStoreFactory = new FileStoreFactory(
                    executorSettings);
            DefaultMessageFactory messageFactory = new DefaultMessageFactory();
            FileLogFactory fileLogFactory = new FileLogFactory(executorSettings);
            socketAcceptor = new SocketAcceptor(application, fileStoreFactory,
                    executorSettings, fileLogFactory, messageFactory);
            socketAcceptor.start();

            SessionID sessionId = (SessionID) socketAcceptor.getSessions().get(0);
            application.onLogon(sessionId);
            int[] i = {1, 2, 3, 4, 5};

            // application.fromApp(new Message(i), sessionId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
