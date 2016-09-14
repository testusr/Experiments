package smeo.experiments.quickfixj;

import quickfix.*;

import java.io.FileInputStream;

/**
 * Created by smeo on 13.09.16.
 */
public class QuickFixJEngineStarter {

    public static void main(String args[]) throws Exception {
        if (args.length != 1) return;
        String fileName = args[0];

        // FooApplication is your class that implements the Application interface
        Application application = new QuickFixJApp();

        SessionSettings settings = new SessionSettings(new FileInputStream(fileName));
        MessageStoreFactory storeFactory = new FileStoreFactory(settings);
        LogFactory logFactory = new FileLogFactory(settings);
        MessageFactory messageFactory = new DefaultMessageFactory();
        Acceptor acceptor = new SocketAcceptor
                (application, storeFactory, settings, logFactory, messageFactory);
        acceptor.start();
        // while(condition == true) { do something; }
        acceptor.stop();
    }
}
