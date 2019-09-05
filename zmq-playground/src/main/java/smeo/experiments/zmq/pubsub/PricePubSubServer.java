package smeo.experiments.zmq.pubsub;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import smeo.experiments.zmq.efficient.bytecontainer.ByteContainer;
import smeo.experiments.zmq.efficient.bytecontainer.ByteContainerObjectOutput;
import smeo.experiments.zmq.model.Price;
import smeo.experiments.zmq.model.StreamId;

import java.io.IOException;

public class PricePubSubServer {
    private ZMQ.Socket publisher;
    ByteContainer byteContainer = ByteContainer.byteArrayContainer(10 * 1024);
    ByteContainerObjectOutput out = new ByteContainerObjectOutput(byteContainer);
    private ZContext context;

    public void init(){
        try {
            this.context = new ZContext();
            this.publisher = context.createSocket(SocketType.PUB);
            publisher.bind("tcp://*:5556");
            System.out.println("server initialized");
        } catch (Exception e) {
            e.printStackTrace();
            stop();
        }

    }
    public void updatePrice(StreamId streamId, Price price){
        byteContainer.clear();
        try {
            System.out.printf(".");
            streamId.writeExternal(out);
            publisher.send(byteContainer.bytes(), 0, byteContainer.size(), ZMQ.SNDMORE);
            byteContainer.clear();
            price.writeExternal(out);
            publisher.send(byteContainer.bytes(), 0, byteContainer.size(), ZMQ.NOBLOCK);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void stop(){
        context.close();
    }

    public static void main(String[] args) throws InterruptedException {
        PricePubSubServer pricePubSubServer = new PricePubSubServer();
        pricePubSubServer.init();

        StreamId streamIdA = StreamId.create("Stream_A");
        Price priceA = Price.random();

        while (!Thread.interrupted()) {
            pricePubSubServer.updatePrice(streamIdA, priceA);
            Thread.sleep(1000);
        }
        pricePubSubServer.stop();
    }
}
