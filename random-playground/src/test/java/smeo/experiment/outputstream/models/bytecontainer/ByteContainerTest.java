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

    @Test
    public void readWriteChar() {
        List<Character> testSamples = randomCharacter(NO_OF_SAMPLES);
        for (Character currSample : testSamples) {
            byteContainer.writeChar(currSample);
        }
        Assert.assertEquals(NO_OF_SAMPLES, byteContainer.size() / NO_OF_BYTES_CHAR);
        for (Character currSample : testSamples) {
            Assert.assertTrue(currSample.charValue() == byteContainer.readChar());
        }
    }

    @Test
    public void readWriteDouble() {
        List<Double> testSamples = randomDouble(NO_OF_SAMPLES);
        for (Double currSample : testSamples) {
            byteContainer.writeDouble(currSample);
        }
        Assert.assertEquals(NO_OF_SAMPLES, byteContainer.size() / NO_OF_BYTES_DOUBLE);
        for (Double currSample : testSamples) {
            Assert.assertTrue(currSample.doubleValue() == byteContainer.readDouble());
        }
    }

    @Test
    public void readWriteShort() {
        List<Short> testSamples = randomShort(NO_OF_SAMPLES);
        for (Short currSample : testSamples) {
            byteContainer.writeShort(currSample);
        }
        Assert.assertEquals(NO_OF_SAMPLES, byteContainer.size() / NO_OF_BYTES_CHAR);
        for (Short currSample : testSamples) {
            Assert.assertTrue(currSample.shortValue() == byteContainer.readShort());
        }
    }

    @Test
    public void readWriteArray() {
        byte[] sampleData = new byte[NO_OF_SAMPLES];
        for (int i = 0; i < NO_OF_SAMPLES; i++) {
            sampleData[0] = (byte) i;
        }

        final int writtenLength = NO_OF_SAMPLES / 2;
        byteContainer.write(sampleData, 0, writtenLength);
        Assert.assertEquals(writtenLength, byteContainer.size());

        byte[] readData = new byte[writtenLength];
        byteContainer.read(readData, 0, NO_OF_SAMPLES);
        for (int i = 0; i < writtenLength; i++) {
            Assert.assertEquals(sampleData[i], readData[i]);
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

    private List<Character> randomCharacter(int noOfSamples) {
        List<Character> elements = new ArrayList<Character>();
        for (int i = 0; i < noOfSamples; i++) {
            elements.add((char) random.nextInt(255));
        }
        return elements;
    }

    private List<Double> randomDouble(int noOfSamples) {
        List<Double> elements = new ArrayList<Double>();
        for (int i = 0; i < noOfSamples; i++) {
            elements.add(random.nextDouble());
        }
        return elements;
    }


    private List<Short> randomShort(int noOfSamples) {
        List<Short> elements = new ArrayList<Short>();
        for (int i = 0; i < noOfSamples; i++) {
            elements.add((short) random.nextInt());
        }
        return elements;
    }
}