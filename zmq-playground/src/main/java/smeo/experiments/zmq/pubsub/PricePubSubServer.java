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
         //   System.out.printf(".");
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
        StreamId streamIdB = StreamId.create("Stream_B");
        StreamId streamIdC = StreamId.create("Stream_C");
        Price priceA = Price.random();
        Price priceB = Price.random();
        Price priceC = Price.random();

        long id = 0;
        while (!Thread.interrupted()) {
            priceA.setId(id++);
            priceB.setId(id++);
            priceC.setId(id++);
            pricePubSubServer.updatePrice(streamIdA, priceA);
            pricePubSubServer.updatePrice(streamIdB, priceB);
            pricePubSubServer.updatePrice(streamIdC, priceC);
            Thread.sleep(1);
        }
        pricePubSubServer.stop();
    }
}
