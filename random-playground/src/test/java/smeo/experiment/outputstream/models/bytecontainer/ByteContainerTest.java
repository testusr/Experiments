package smeo.experiment.outputstream.models.bytecontainer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static smeo.experiment.outputstream.models.bytecontainer.ByteContainer.*;

/**
 * Created by smeo on 22.03.17.
 */
public class ByteContainerTest {
    public static final int NO_OF_SAMPLES = 100;
    ByteContainer byteContainer;
    Random random = new Random();

    @Before
    public void setup() {
        byteContainer = new ByteContainer();
    }

    @Test
    public void readWriteByte() {
        List<Byte> testSamples = randomBytes(NO_OF_SAMPLES);
        for (Byte randomByte : testSamples) {
            byteContainer.writeByte(randomByte);
        }
        Assert.assertEquals(NO_OF_SAMPLES, byteContainer.size());
        for (Byte randomByte : testSamples) {
            Assert.assertTrue(randomByte.byteValue() == byteContainer.readByte());
        }
    }

    @Test
    public void readWriteInt() {
        List<Integer> testSamples = randomInts(NO_OF_SAMPLES);
        for (Integer currSample : testSamples) {
            byteContainer.writeInt(currSample);
        }
        Assert.assertEquals(NO_OF_SAMPLES, byteContainer.size() / NO_OF_BYTES_INT);
        for (Integer currSample : testSamples) {
            Assert.assertTrue(currSample.intValue() == byteContainer.readInt());
        }
    }

    @Test
    public void readWriteFloat() {
        List<Float> testSamples = randomFloat(NO_OF_SAMPLES);
        for (Float currSample : testSamples) {
            byteContainer.writeFloat(currSample);
        }
        Assert.assertEquals(NO_OF_SAMPLES, byteContainer.size() / NO_OF_BYTES_FLOAT);
        for (Float currSample : testSamples) {
            Assert.assertTrue(currSample.floatValue() == byteContainer.readFloat());
        }
    }

    @Test
    public void readWriteBoolean() {
        List<Boolean> testSamples = randomBoolean(NO_OF_SAMPLES);
        for (Boolean currSample : testSamples) {
            byteContainer.writeBoolean(currSample);
        }
        Assert.assertEquals(NO_OF_SAMPLES, byteContainer.size() / NO_OF_BYTES_BOOLEAN);
        for (Boolean currSample : testSamples) {
            Assert.assertTrue(currSample.booleanValue() == byteContainer.readBoolean());
        }
    }


    private List<Byte> randomBytes(int noOfSamples) {
        List<Byte> elements = new ArrayList<Byte>();
        for (int i = 0; i < noOfSamples; i++) {
            elements.add((byte) random.nextInt(255));
        }
        return elements;
    }

    private List<Integer> randomInts(int noOfSamples) {
        List<Integer> elements = new ArrayList<Integer>();
        for (int i = 0; i < noOfSamples; i++) {
            elements.add(random.nextInt());
        }
        return elements;
    }

    private List<Float> randomFloat(int noOfSamples) {
        List<Float> elements = new ArrayList<Float>();
        for (int i = 0; i < noOfSamples; i++) {
            elements.add(random.nextFloat());
        }
        return elements;
    }

    private List<Boolean> randomBoolean(int noOfSamples) {
        List<Boolean> elements = new ArrayList<Boolean>();
        for (int i = 0; i < noOfSamples; i++) {
            elements.add(random.nextBoolean());
        }
        return elements;
    }
}