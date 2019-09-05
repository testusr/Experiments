package smeo.experiments.zmq.model;

import lombok.Data;
import smeo.experiments.zmq.efficient.EfficientString;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

@Data
public class StreamId implements Externalizable {
    EfficientString productId = new EfficientString();

    public static StreamId create(String productId){
        StreamId newSubscription = new StreamId();
        newSubscription.productId.encodeUTF8(productId);
        return newSubscription;
    }

    String getProductId(){
        return productId.decode();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        productId.writeExternal(out);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        productId.readExternal(in);
    }
}
