package smeo.experiments.simplefix.server;

import smeo.experiments.simplefix.model.FixMessage;
import smeo.experiments.simplefix.model.SimpleFixMessageParser;

/**
 * Created by truehl on 15.08.17.
 */
public class StartSimpleFixServer {
    public static final int LISTEN_TO_PORT = 5001;


    public static void main(String[] args) {
        SimpleFixServer simpleFixServer = new SimpleFixServer(LISTEN_TO_PORT, "localhost");

        SimpleSessionConfig clientSessionConfig = SimpleSessionConfig.builder()
                .beginString("FIXT.1.1")
                .targetCompID("Server.CompID")
                .targetSubID("Server.SubId")
                .senderCompID("Client.CompID")
                .senderSubID("ClientSubId")
                .build();

        SimpleFixSession clientSession = simpleFixServer.initFixClientSession(clientSessionConfig);
        simpleFixServer.startListening();

        FixMessage fixMessage = new FixMessage();
        System.out.println("[STARTER] waiting for session to connect");

        while (!clientSession.isConnected()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("[STARTER] start sending snapshots");

        //     String originalFixMessage = "8=FIX.4.2\u00019=178\u000135=8\u000134=2\u000149=Server.CompID\u000150=Server.SubId\u000152=20180220-05:30:16.961\u000156=Client.CompID\u000157=ClientSubId\u00016=0\u000111=1\u000114=0\u000117=execId\u000120=0\u000137=orderId\u000139=0\u000144=1\u000154=1\u000155=EUR/USD\u0001150=2\u0001151=0\u0001";
        String originalFixMessage = "8=FIXT.1.1\u00019=418\u000135=W\u000155=USD/CAD\u0001262=USD/CAD\u0001268=4\u0001269=0\u0001270=1.25806\u0001271=1000000\u0001272=20180213\u0001273=00:00:00.279\u000164=20180306\u00011070=1\u0001278=26928496/0/\u0001269=0\u0001270=1.25796\u0001271=10000000\u0001272=20180213\u0001273=00:00:00.279\u000164=20180306\n" +
                "\u00011070=1\u0001278=26928496/1/\u0001269=1\u0001270=1.25822\u0001271=1000000\u0001272=20180213\u0001273=00:00:00.279\u000164=20180306\u00011070=1\u0001278=26928496/20/\u0001269=1\u0001270=1.25832\u0001271=10000000\u0001272=20180213\u0001273=00:00:00.279\u000164=20180306\u00011070=1\u0001278=26928496/21/\u000110=073\u0001";
        SimpleFixMessageParser.parseFromFixString(originalFixMessage, "\u0001", fixMessage);

        new Thread(new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setName("send client messages");
                for (int i = 0; i < 50; i++) {
                    if (clientSession.isConnected()) {
                        clientSession.readMessage();
                        if (clientSession.sendMessage(fixMessage)) {
                        } else {
                            System.out.println("[STARTER] not connected");
                        }
                        try {
                            Thread.sleep(5);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();

        System.out.println("[STARTER] - done sending snapshots");
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
