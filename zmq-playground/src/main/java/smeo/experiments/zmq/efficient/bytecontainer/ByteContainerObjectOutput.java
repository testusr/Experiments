package smeo.experiments.zmq.efficient.bytecontainer;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;
import java.io.ObjectOutput;
import java.io.Serializable;

/**
 * Created by smeo on 21.03.17.
 */
public class ByteContainerObjectOutput implements ObjectOutput {
    private final ByteContainer byteContainer;

    public ByteContainerObjectOutput(ByteContainer byteContainer) {
        this.byteContainer = byteContainer;
    }

    public ByteContainer byteContainer() {
        return byteContainer;
    }

    @Override
    public void writeObject(Object obj) throws IOException {
        final byte[] serialize = SerializationUtils.serialize((Serializable) obj);
        writeInt(serialize.length);
        write(serialize);
    }

    @Override
    public void write(int b) throws IOException {
        byteContainer.writeByte((byte) b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        for (int i = 0; i < b.length; i++) {
            byteContainer.writeByte(b[i]);
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        byteContainer.write(b, off, len);
        throw new NotImplementedException("not implemented");
    }

    @Override
    public void writeBoolean(boolean v) throws IOException {
        byteContainer.writeBoolean(v);
    }

    @Override
    public void writeByte(int v) throws IOException {
        byteContainer.writeByte((byte) v);
    }

    @Override
    public void writeShort(int v) throws IOException {
        byteContainer.writeShort((short) v);
    }

    @Override
    public void writeChar(int v) throws IOException {
        byteContainer.writeChar((char) v);
    }

    @Override
    public void writeInt(int v) throws IOException {
        byteContainer.writeInt(v);
    }

    @Override
    public void writeLong(long v) throws IOException {
        byteContainer.writeLong(v);
    }

    @Override
    public void writeFloat(float v) throws IOException {
        byteContainer.writeFloat(v);
    }

    @Override
    public void writeDouble(double v) throws IOException {
        byteContainer.writeDouble(v);
    }

    @Override
    public void writeBytes(String s) throws IOException {
        throw new NotImplementedException("");
    }

    @Override
    public void writeChars(String s) throws IOException {
        throw new NotImplementedException("");
    }

    @Override
    public void writeUTF(String s) throws IOException {
        throw new NotImplementedException("");
    }

    @Override
    public void flush() throws IOException {

    }

    @Override
    public void close() throws IOException {

    }
}
