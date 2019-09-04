package smeo.experiment.outputstream.models;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Random;

/**
 * Created by smeo on 21.03.17.
 */
public class ExampleClass implements Externalizable {
    private static Random random = new Random();
    double[] exampleArrayA;
    int[] exampleArrayB;

    public static ExampleClass createRandom() {
        final ExampleClass newInstance = new ExampleClass();
        final int arraySizeA = random.nextInt(100);
        newInstance.exampleArrayA = new double[arraySizeA];
        for (int i = 0; i < arraySizeA; i++) {
            newInstance.exampleArrayA[i] = random.nextDouble();
        }
        final int arraySizeB = random.nextInt(100);
        newInstance.exampleArrayB = new int[arraySizeB];
        for (int i = 0; i < arraySizeB; i++) {
            newInstance.exampleArrayB[i] = random.nextInt();
        }
        return newInstance;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(exampleArrayA.length);
        for (int i = 0; i < exampleArrayA.length; i++) {
            out.writeDouble(exampleArrayA[i]);
        }
        out.writeInt(exampleArrayB.length);
        for (int i = 0; i < exampleArrayB.length; i++) {
            out.writeInt(exampleArrayB[i]);
        }

    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int arraySize = in.readInt();
        if (exampleArrayA.length != arraySize) {
            exampleArrayA = new double[arraySize];
        }
        for (int i = 0; i < arraySize; i++) {
            exampleArrayA[i] = in.readDouble();
        }
        arraySize = in.readInt();
        if (exampleArrayB.length != arraySize) {
            exampleArrayB = new int[arraySize];
        }
        for (int i = 0; i < arraySize; i++) {
            exampleArrayB[i] = in.readInt();
        }

    }
}
