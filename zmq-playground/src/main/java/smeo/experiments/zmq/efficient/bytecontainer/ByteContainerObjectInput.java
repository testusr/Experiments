package smeo.experiments.zmq.efficient.bytecontainer;

import org.apache.commons.lang3.SerializationUtils;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.io.ObjectInput;

/**
 * ObjectInput implemeting wrapper for a {@link ByteContainer}
 */
public class ByteContainerObjectInput implements ObjectInput {
    private final ByteContainer byteContainer;

    public ByteContainerObjectInput(ByteContainer byteContainer) {
        this.byteContainer = byteContainer;
    }

    public ByteContainer byteContainer() {
        return byteContainer;
    }


    @Override
    public Object readObject() throws ClassNotFoundException, IOException {
        final int noOfBytes = byteContainer.readInt();
        if (noOfBytes > 0) {
            byte[] rawData = new byte[noOfBytes];
            byteContainer.read(rawData, noOfBytes);
            return SerializationUtils.deserialize(rawData);
        }
        return null;
    }

    @Override
    public int read() throws IOException {
        return byteContainer.readByte();
    }

    @Override
    public int read(byte[] b) throws IOException {
        for (int i = 0; i < b.length; i++) {
            b[i] = byteContainer.readByte();
        }
        return b.length;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return byteContainer.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return byteContainer.skipRead(n);
    }

    @Override
    public int available() throws IOException {
        throw new NotImplementedException();
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public void readFully(byte[] b) throws IOException {
        throw new NotImplementedException();
    }

    @Override
    public void readFully(byte[] b, int off, int len) throws IOException {
        throw new NotImplementedException();
    }

    @Override
    public int skipBytes(int n) throws IOException {
        throw new NotImplementedException();
    }

    @Override
    public boolean readBoolean() throws IOException {
        return byteContainer.readBoolean();
    }

    @Override
    public byte readByte() throws IOException {
        return byteContainer.readByte();
    }

    @Override
    public int readUnsignedByte() throws IOException {
        throw new NotImplementedException();
    }

    @Override
    public short readShort() throws IOException {
        return byteContainer.readShort();
    }

    @Override
    public int readUnsignedShort() throws IOException {
        throw new NotImplementedException();
    }

    @Override
    public char readChar() throws IOException {
        return byteContainer.readChar();
    }

    @Override
    public int readInt() throws IOException {
        return byteContainer.readInt();
    }

    @Override
    public long readLong() throws IOException {
        return byteContainer.readLong();
    }

    @Override
    public float readFloat() throws IOException {
        return byteContainer.readFloat();
    }

    @Override
    public double readDouble() throws IOException {
        return byteContainer.readDouble();
    }

    @Override
    public String readLine() throws IOException {
        throw new NotImplementedException();
    }

    @Override
    public String readUTF() throws IOException {
        throw new NotImplementedException();
    }
}
