package smeo.experiment.outputstream.models.bytecontainer;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.io.ObjectOutput;

/**
 * Created by smeo on 21.03.17.
 */
public class ByteContainerObjectOutput implements ObjectOutput {
    private ByteContainer byteContainer;

    public ByteContainerObjectOutput(ByteContainer byteContainer) {
        this.byteContainer = byteContainer;
    }

    @Override
    public void writeObject(Object obj) throws IOException {
        throw new NotImplementedException();
    }

    @Override
    public void write(int b) throws IOException {
        byteContainer.writeByte((byte)b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        for (int i=0; i < b.length; i++){
            byteContainer.writeByte(b[i]);
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        throw new NotImplementedException();
    }

    @Override
    public void writeBoolean(boolean v) throws IOException {
        byteContainer.writeBoolean(v);
    }

    @Override
    public void writeByte(int v) throws IOException {
        byteContainer.writeByte((byte)v);
    }

    @Override
    public void writeShort(int v) throws IOException {
        byteContainer.writeShort((short)v);
    }

    @Override
    public void writeChar(int v) throws IOException {
        byteContainer.writeChar((char)v);
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
        throw new NotImplementedException();
    }

    @Override
    public void writeChars(String s) throws IOException {
        throw new NotImplementedException();
    }

    @Override
    public void writeUTF(String s) throws IOException {
        throw new NotImplementedException();
    }

    @Override
    public void flush() throws IOException {

    }

    @Override
    public void close() throws IOException {

    }
}
