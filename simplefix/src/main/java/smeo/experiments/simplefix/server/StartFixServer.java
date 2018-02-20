package smeo.experiments.simplefix.server;

import smeo.experiments.simplefix.model.FixMessage;
import smeo.experiments.simplefix.model.SimpleFixMessageParser;

/**
 * Created by truehl on 15.08.17.
 */
public class StartFixServer {
    public static final int LISTEN_TO_PORT = 5001;


    public static void main(String[] args) {
        SimpleFixServer simpleFixServer = new SimpleFixServer(LISTEN_TO_PORT, "localhost");

        SimpleSessionConfig clientSessionConfig = SimpleSessionConfig.builder()
                .beginString("FIX.4.2")
                .targetCompID("Server.CompID")
                .targetSubID("Server.SubId")
                .senderCompID("Client.CompID")
                .senderSubID("ClientSubId")
                .build();

        SimpleFixSession clientSession = simpleFixServer.initFixClientSession(clientSessionConfig);
        simpleFixServer.startListening();

        FixMessage fixMessage = new FixMessage();

        while (!clientSession.isConnected()) {
            System.out.println("waiting for session to connect");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("start sending snapshots");

        String originalFixMessage = "8=FIX.4.2\u00019=178\u000135=8\u000134=2\u000149=Server.CompID\u000150=Server.SubId\u000152=20180220-05:30:16.961\u000156=Client.CompID\u000157=ClientSubId\u00016=0\u000111=1\u000114=0\u000117=execId\u000120=0\u000137=orderId\u000139=0\u000144=1\u000154=1\u000155=EUR/USD\u0001150=2\u0001151=0\u0001";
        SimpleFixMessageParser.parseFromFixString(originalFixMessage, "\u0001", fixMessage);

        for (int i = 0; i < 30; i++) {
            if (clientSession.isConnected()) {
                if (clientSession.sendMessage(fixMessage)) {
                    System.out.println("is connected");
                } else {
                    System.out.println("not connected");
                }
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        while (true) {
            simpleFixServer.readMessages();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

}
