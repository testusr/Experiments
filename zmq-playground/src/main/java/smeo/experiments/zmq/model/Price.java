package smeo.experiments.zmq.model;

import lombok.Data;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

@Data
public class Price implements Externalizable {
    Product product;
    double bidRate;
    double askRate;
    CurrencyCouple currencyCouple;

    private Price(){};

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(product.type);
        out.writeDouble(bidRate);
        out.writeDouble(askRate);
        out.writeInt(currencyCouple.ordinal());
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException {
        product.type = in.readInt();
        bidRate = in.readDouble();
        askRate = in.readDouble();
        currencyCouple = CurrencyCouple.values()[in.readInt()];
    }

    public static Price empty(){
        Price newPrice = new Price();
        newPrice.product = new Product();
        return newPrice;
    }

    public static Price random(){
        Price newPrice = new Price();
        newPrice.product = new Product();
        return newPrice;
    }

}
