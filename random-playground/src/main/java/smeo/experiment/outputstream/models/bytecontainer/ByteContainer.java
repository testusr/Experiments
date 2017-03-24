package smeo.experiment.outputstream.models.bytecontainer;

/**
 * Reading and writing primitives to and from byte array
 */
public class ByteContainer {
    public static final int NO_OF_BYTES_INT = 4;
    public static final int NO_OF_BYTES_LONG = 8;
    public static final int NO_OF_BYTES_DOUBLE = NO_OF_BYTES_LONG;
    public static final int NO_OF_BYTES_FLOAT = NO_OF_BYTES_INT;
    public static final int NO_OF_BYTES_SHORT = 2;
    public static final int NO_OF_BYTES_BOOLEAN = 1;
    public static final int NO_OF_BYTES_CHAR = 2;


    private int DEFAULT_BLOCK_SIZE = 1024;
    byte[][] data = new byte[100][];

    int blockSize = DEFAULT_BLOCK_SIZE;
    int allocatedBlocks = 0;
    long capacity = 0;

    int writePos = 0;
    int readPos = 0;

    private void grow(long noOfNeededBytes) {
        if (allocatedBlocks + 1 == data.length) {
            throw new RuntimeException("max capacity reached");
        }
        long leftBytesAfterAdd = capacity - allocatedBlocks - noOfNeededBytes;
        if (leftBytesAfterAdd < 0) {
            int neededBlocks = (int) ((-leftBytesAfterAdd) / blockSize) + 1;
            for (int i = 0; i < neededBlocks; i++) {
                data[allocatedBlocks++] = new byte[blockSize];
                capacity += blockSize;
            }
        }
    }

    public ByteContainer(long capacity, int blockSize) {
        this.blockSize = blockSize;
        grow(capacity);
    }

    public ByteContainer(long capacity) {
        grow(capacity);
    }

    public ByteContainer() {
        grow(DEFAULT_BLOCK_SIZE);
    }


    public long capacity() {
        return capacity;
    }
    
    /*
     * Methods for unpacking primitive values from byte arrays starting at
     * given offsets.
     */

    public boolean readBoolean() {
        return byteAt(readPos++) != 0;
    }

    private byte byteAt(int index) {
        return data[index / blockSize][index % blockSize];
    }

    public char readChar() {
        char result = (char) ((byteAt(readPos + 1) & 0xFF) +
                (byteAt(readPos) << 8));
        readPos += NO_OF_BYTES_CHAR;
        return result;
    }

    public short readShort() {
        short result = (short) ((byteAt(readPos + 1) & 0xFF) +
                (byteAt(readPos) << 8));
        readPos += NO_OF_BYTES_SHORT;
        return result;
    }

    public byte readByte() {
        return byteAt(readPos++);
    }


    public int readInt() {
        int result = ((byteAt(readPos + 3) & 0xFF)) +
                ((byteAt(readPos + 2) & 0xFF) << 8) +
                ((byteAt(readPos + 1) & 0xFF) << 16) +
                ((byteAt(readPos)) << 24);
        readPos += 4;
        return result;

    }

    public float readFloat() {
        return Float.intBitsToFloat(readInt());
    }

    public long readLong() {
        long result = ((byteAt(readPos + 7) & 0xFFL)) +
                ((byteAt(readPos + 6) & 0xFFL) << 8) +
                ((byteAt(readPos + 5) & 0xFFL) << 16) +
                ((byteAt(readPos + 4) & 0xFFL) << 24) +
                ((byteAt(readPos + 3) & 0xFFL) << 32) +
                ((byteAt(readPos + 2) & 0xFFL) << 40) +
                ((byteAt(readPos + 1) & 0xFFL) << 48) +
                (((long) byteAt(readPos)) << 56);
        readPos += 8;
        return result;
    }

    public double readDouble() {
        return Double.longBitsToDouble(readLong());
    }

    /*
     * Methods for packing primitive values into byte arrays starting at given
     * offsets.
     */

    public void writeByte(byte towrite) {

        b(writePos++, towrite);
    }

    public void writeBoolean(boolean val) {
        b(writePos, (byte) (val ? 1 : 0));
        writePos++;
    }

    public void writeChar(char val) {
        b(writePos + 1, (byte) (val));
        b(writePos, (byte) (val >>> 8));
        writePos += 2;
    }

    public void writeShort(short val) {
        b(writePos + 1, (byte) val);
        b(writePos, (byte) (val >>> 8));
        writePos += NO_OF_BYTES_SHORT;
    }

    public void writeInt(int val) {
        b(writePos + 3, (byte) val);
        b(writePos + 2, (byte) (val >>> 8));
        b(writePos + 1, (byte) (val >>> 16));
        b(writePos, (byte) (val >>> 24));
        writePos += NO_OF_BYTES_INT;
    }

    public void writeFloat(float val) {
        writeInt(Float.floatToIntBits(val));
    }

    public void writeLong(long val) {
        b(writePos + 7, (byte) val);
        b(writePos + 6, (byte) (val >>> 8));
        b(writePos + 5, (byte) (val >>> 16));
        b(writePos + 4, (byte) (val >>> 24));
        b(writePos + 3, (byte) (val >>> 32));
        b(writePos + 2, (byte) (val >>> 40));
        b(writePos + 1, (byte) (val >>> 48));
        b(writePos, (byte) (val >>> 56));
        writePos += NO_OF_BYTES_LONG;
    }

    private void b(int writPos, byte value) {
        data[writPos / blockSize][writPos % blockSize] = value;
    }

    public void writeDouble(double val) {
        writeLong(Double.doubleToLongBits(val));
    }

    public int size() {
        return writePos;
    }

    public long skipRead(long n) {
        final long skippedBytes = Math.max(n, leftRead());
        readPos += skippedBytes;
        return skippedBytes;
    }

    public long skipWrite(long n, boolean extendIfExceeding) {
        long capacityLeftAfterSkip = leftWrite() - n;
        if (capacityLeftAfterSkip < 0) {
            if (extendIfExceeding) {
                grow(Math.abs(capacityLeftAfterSkip));
            } else {
                return capacityLeftAfterSkip + n;
            }
        }
        return n;
    }

    /**
     * @return no of bytes left to read
     */
    public long leftRead() {
        return writePos - readPos;
    }

    /**
     * @return number of bytes possible to write with the current capacity
     */

    public long leftWrite() {
        return capacity - writePos;
    }

    public int read(byte[] b, int off, int len) {
        final int bytesToRead = Math.min(writePos - off, len);
        if (bytesToRead < 0) {
            return 0;
        } else {
            for (int i = 0; i < bytesToRead; i++) {
                b[i] = byteAt(off + i);
            }
        }
        return bytesToRead;
    }

    public void write(byte[] b, int off, int len) {
        for (int i = 0; i < len; i++) {
            final int pos = off + i;
            if (pos > capacity) {
                grow(blockSize);
            }
            b(writePos, b[i]);
            writePos++;
        }

    }
}
