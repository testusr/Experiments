package smeo.experiments.zmq.pubsub;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import smeo.experiments.zmq.efficient.bytecontainer.ByteContainer;
import smeo.experiments.zmq.efficient.bytecontainer.ByteContainerObjectInput;
import smeo.experiments.zmq.efficient.bytecontainer.ByteContainerObjectOutput;
import smeo.experiments.zmq.model.Price;
import smeo.experiments.zmq.model.StreamId;

import java.io.IOException;
import java.nio.ByteBuffer;

public class PricePubSubClient {
    private ZMQ.Socket subscriber;
    ByteContainer outByteContainer = new ByteContainer(10 * 1024);
    ByteContainer inByteContainer = new ByteContainer(10 * 1024);
    ByteContainerObjectOutput out = new ByteContainerObjectOutput(outByteContainer);
    ByteContainerObjectInput in = new ByteContainerObjectInput(inByteContainer);

    StreamId inStreamId = new StreamId();
    Price inPrice = Price.empty();


    private ByteBuffer receiveBuffer = ByteBuffer.allocate(10*1024);
    private ZContext context;

    public void init(){
        try {
        this.context = new ZContext();
            this.subscriber = context.createSocket(SocketType.SUB);
            subscriber.connect("tcp://localhost:5556");
        } catch (Exception e) {
            e.printStackTrace();
            stop();
        }

    }

    private void stop() {
        context.close();
    }

    public void subscribe(StreamId streamId){
        try {
            subscriber.subscribe(asBytes(streamId));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readAndPrintMessage(){
        while (true) {
            try {

                receiveBuffer.clear();
                subscriber.recvByteBuffer(receiveBuffer, ZMQ.NOBLOCK);
                if (receiveBuffer.position() > 0) {
                    inByteContainer.clear();
                    inByteContainer.write(receiveBuffer.array(), 0, receiveBuffer.position());
                    inStreamId.readExternal(in);

                    receiveBuffer.clear();
                    subscriber.recvByteBuffer(receiveBuffer, ZMQ.NOBLOCK);
                    inByteContainer.write(receiveBuffer.array(), 0, receiveBuffer.position());
                    inPrice.readExternal(in);

      //              System.out.println("stream : " + inStreamId.toString());
      //              System.out.println("price : " + inPrice.toString());
                }
            } catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    private byte[] asBytes(StreamId streamId) throws IOException {
        streamId.writeExternal(out);
        byte[] bytes = new byte[outByteContainer.size()];
        for (int i=0; i < bytes.length; i++){
            bytes[i] = outByteContainer.readByte();
        }
        return bytes;
    }

    public static void main(String[] args) {
        PricePubSubClient pricePubSubClient = new PricePubSubClient();
        pricePubSubClient.init();
        StreamId streamIdA = StreamId.create("Stream_A");
        StreamId streamIdB = StreamId.create("Stream_B");
        StreamId streamIdC = StreamId.create("Stream_C");
        pricePubSubClient.subscribe(streamIdA);
        pricePubSubClient.subscribe(streamIdB);
        pricePubSubClient.subscribe(streamIdC);
        while(!Thread.interrupted()){
            pricePubSubClient.readAndPrintMessage();
        }
    }
}
