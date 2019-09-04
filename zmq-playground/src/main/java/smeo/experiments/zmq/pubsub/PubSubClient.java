package smeo.experiments.zmq.pubsub;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.Random;

public class PubSubClient {
    public static void main(String[] args)
    {
        try (ZContext context = new ZContext()) {
            ZMQ.Socket subscriber = context.createSocket(SocketType.SUB);
            if (args.length == 1)
                subscriber.connect(args[0]);
            else subscriber.connect("tcp://localhost:5556");

            //Random rand = new Random(System.currentTimeMillis());
            //String subscription = String.format("%03d", rand.nextInt(1000));
            //subscriber.subscribe(subscription.getBytes(ZMQ.CHARSET));

            for (int i=0; i < 1000; i++) {
                String subscription = String.format("%03d", i);
                subscriber.subscribe(subscription.getBytes(ZMQ.CHARSET));
            }

            while (true) {
                String topic = subscriber.recvStr();
                if (topic == null)
                    break;
                String data = subscriber.recvStr();
                //assert (topic.equals(subscription));
                System.out.println(topic+": " + data);
            }
        }
    }
}
