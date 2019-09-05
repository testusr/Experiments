package smeo.experiments.zmq.model;

import lombok.Data;
import lombok.Getter;
import lombok.ToString;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

@Data
@ToString
public class Price implements Externalizable {
    final static AtomicLong lastId = new AtomicLong(0);
    final static Random random = new Random(System.nanoTime());

    long id;
    double bidRate;
    double askRate;
    CurrencyCouple currencyCouple;

    private Price(){};

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(id);
        out.writeDouble(bidRate);
        out.writeDouble(askRate);
        out.writeInt(currencyCouple.ordinal());
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException {
        id = in.readLong();
        bidRate = in.readDouble();
        askRate = in.readDouble();
        currencyCouple = CurrencyCouple.values()[in.readInt()];
    }

    public static Price empty(){
        Price newPrice = new Price();
        newPrice.id = lastId.incrementAndGet();
        return newPrice;
    }

    public static Price random(){
        Price newPrice = new Price();
        newPrice.id = lastId.incrementAndGet();
        newPrice.bidRate = random.nextDouble();
        newPrice.askRate = random.nextDouble();
        newPrice.currencyCouple = CurrencyCouple.EURGBP;
        return newPrice;
    }

}
