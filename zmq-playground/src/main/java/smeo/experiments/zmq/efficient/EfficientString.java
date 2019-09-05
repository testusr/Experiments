package smeo.experiments.zmq.efficient;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Object to efficiently represent id. Efficient serialization and efficient comparison.
 */
public class EfficientString implements EfficientObject<EfficientString> {
    public static final int BYTES_PER_LONG = 8;
    private static final int BITS_PER_BYTE = 8;
    long[] idRepresentation = new long[0];
    int noOfRepresenedBytes = 0;

    public EfficientString(String string) {
        encodeUTF8(string);
    }

    public EfficientString() {
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(noOfRepresenedBytes);
        int noOfUsedLongs = noOfNeededLongs(noOfRepresenedBytes);
        for (int i = 0; i < noOfUsedLongs; i++) {
            out.writeLong(idRepresentation[i]);
        }
    }

    int noOfUsedLongs() {
        return noOfNeededLongs(noOfRepresenedBytes);
    }

    private static int noOfNeededLongs(int noOfBytes) {
        return (noOfBytes + (BYTES_PER_LONG - 1)) / BYTES_PER_LONG;
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.noOfRepresenedBytes = in.readInt();
        int noOfElements = noOfNeededLongs(noOfRepresenedBytes);
        if (noOfElements > idRepresentation.length) {
            idRepresentation = new long[noOfElements];
        }
        for (int i = 0; i < noOfElements; i++) {
            idRepresentation[i] = in.readLong();
        }
    }

    @Override
    public void internalize(EfficientString src) {
        this.noOfRepresenedBytes = src.noOfRepresenedBytes;
        int noNeededLongs = noOfNeededLongs(noOfRepresenedBytes);
        if (noNeededLongs > idRepresentation.length) {
            idRepresentation = new long[noNeededLongs];
        }
        for (int i = 0; i < noNeededLongs; i++) {
            idRepresentation[i] = src.idRepresentation[i];
        }
    }

    public String decode() {
        final ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);

        byte[] targetBytes = new byte[noOfRepresenedBytes];
        for (int i = 0; i < idRepresentation.length; i++) {
            decodeLong(idRepresentation[i], i * Long.BYTES, targetBytes, buffer);
        }
        return new String(targetBytes);
    }

    private void decodeLong(long longToTranslate, int startIndex, byte[] targetBytes, ByteBuffer buffer) {
        byte[] longByteArray = buffer.putLong(0, longToTranslate).array();
        int bytesToWrite = Math.min(targetBytes.length - startIndex, Long.BYTES);
        int longArrayStartIndex = (longByteArray.length - bytesToWrite);
        for (int i = 0; i < bytesToWrite; i++) {
            targetBytes[startIndex + i] = longByteArray[longArrayStartIndex + i];
        }
    }

    /**
     * long 64 bits => 8 bytes
     *
     * @param id
     * @return
     */
    public void encodeUTF8(String id) {
        if (id == null || id.isEmpty()) {
            noOfRepresenedBytes = 0;
            return;
        }

        final byte[] bytes = id.getBytes(Charset.forName("UTF-8"));
        int noOfneededLongs = noOfNeededLongs(bytes.length);
        if (idRepresentation.length < noOfneededLongs) {
            idRepresentation = new long[noOfneededLongs];
        }

        int i = 0;
        long encodedLong = 0;
        while (i < bytes.length) {
            if (i != 0) {
                encodedLong <<= BITS_PER_BYTE;
            }
            encodedLong |= bytes[i++];
            if (i % BYTES_PER_LONG == 0) {
                idRepresentation[(i - 1) / BYTES_PER_LONG] = encodedLong;
                encodedLong = 0L;
            }
        }
        if (bytes.length % BYTES_PER_LONG != 0) {
            idRepresentation[noOfneededLongs - 1] = encodedLong;
        }
        noOfRepresenedBytes = bytes.length;
    }

    public String toString() {
        return decode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EfficientString that = (EfficientString) o;
        if (noOfRepresenedBytes != that.noOfRepresenedBytes) return false;
        for (int i = 0; i < noOfUsedLongs(); i++) {
            if (idRepresentation[i] != that.idRepresentation[i]) return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        if (idRepresentation == null)
            return 0;

        int result = 1;
        for (int i = 0; i < noOfUsedLongs(); i++) {
            long element = idRepresentation[i];
            int elementHash = (int) (element ^ (element >>> 32));
            result = 31 * result + elementHash;
        }

        return result;
    }
}
