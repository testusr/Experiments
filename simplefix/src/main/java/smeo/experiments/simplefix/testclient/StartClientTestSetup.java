package smeo.experiments.simplefix.testclient;

import quickfix.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class StartClientTestSetup {
    List<TestClientRunner> testClients = new ArrayList<>();

    public static void main(String[] args) {
        StartClientTestSetup startClientTestSetup = new StartClientTestSetup();
        startClientTestSetup.startClient();
//        startClientTestSetup.startClient();
//        startClientTestSetup.startClient();
//        startClientTestSetup.startClient();
//        startClientTestSetup.startClient();
//        startClientTestSetup.startClient();
//        startClientTestSetup.startClient();
//        startClientTestSetup.startClient();
//        startClientTestSetup.startClient();

    }

    private void startClient() {
        TestClientRunner testClientRunner = new TestClientRunner(testClients.size() + 1);
        testClients.add(testClientRunner);
        new Thread(testClientRunner).start();
    }

    private static class TestClientRunner implements Runnable {
        public TestClientRunner(int clientId) {
            this.clientId = clientId;
        }

        final int clientId;
        boolean isRunning = true;

        public void stop() {
            isRunning = false;
        }

        @Override
        public void run() {
            SocketInitiator socketInitiator = null;
            try {
                InputStream is = SimpleQuickFixClient.class.getResourceAsStream("/client" + clientId + ".cfg");
                SessionSettings initiatorSettings = new SessionSettings(is);
                Application initiatorApplication = new SimpleQuickFixClient(clientId);
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
                    System.out.println("[CLIENT - " + clientId + "] Waiting for login success");
                    Thread.sleep(1000);
                }
                System.out.println("[CLIENT - " + clientId + "] Logged In...");

                Thread.sleep(5000);

                while (isRunning) {
                    Thread.sleep(100);
                }
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
}
