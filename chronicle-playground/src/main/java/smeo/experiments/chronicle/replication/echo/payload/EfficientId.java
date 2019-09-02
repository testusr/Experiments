package smeo.experiments.chronicle.replication.echo.payload;

import org.jetbrains.annotations.NotNull;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;

/**
 * Translation from a immutable string id into a resuable long based id container.
 */

public class EfficientId implements Externalizable, Comparable<EfficientId> {
	public static final int MAX_ID_LENGTH = Byte.MAX_VALUE * 8;
	public static final long BYTE_MASK = 255;
	private int size;
	private long[] encoded;
	private transient int cashedHashCode = -1;
	private transient boolean isHashCodeCached = false;

	public EfficientId() {
		this.encoded = new long[0];
		this.size = encoded.length;
	}

	/**
	 * The id should not contain any spaces, otherwise decoding to the original might not work.
	 * This is not checked for performance reasons !
	 * 
	 * @param id
	 */
	public EfficientId(final String id) {
		this.size = neededArraySize(id);
		this.encoded = new long[size];
		encode(id, this.encoded);
	}

	public static int neededArraySize(String id) {
		assert (id != null);
		assert (id.length() < MAX_ID_LENGTH);

		final double exact = id.length() / 8.0f;
		int neededSize = (int) exact;
		if ((exact - neededSize) != 0.0) {
			neededSize++;
		}
		return neededSize;
	}

	/**
	 * econding a string to a long array, relying on tha the string only contains ascii characters
	 * 
	 * @param id
	 * @param toReuse
	 * @return
	 */
	public static void encode(String id, long[] toReuse) {
		if (!id.isEmpty()) {
			final int idLength = id.length();
			final int elementCount = neededArraySize(id);
			if (toReuse.length < elementCount) {
				throw new IllegalArgumentException("array to be reused to small");
			}

			long currEncodedElement = 0;
			int resultArrayIndex = 0;
			for (int i = 0; i < id.length(); i++) {
				if (i < id.length()) {
					final int longByteIndex = i % Long.BYTES;
					if (longByteIndex == 0) {
						if (i > 0) {
							toReuse[resultArrayIndex++] = currEncodedElement;
						}
						currEncodedElement = 0;
					}
					currEncodedElement += ((long) ((byte) id.charAt(i))) << longByteIndex * Long.BYTES;
				}
			}
			toReuse[resultArrayIndex++] = currEncodedElement;

			// set left over elements of preallocated array to 0
			while (resultArrayIndex < toReuse.length) {
				toReuse[resultArrayIndex++] = 0;
			}
		}
	}

	/**
	 * DO NOT USE FOR TIME CRITICAL WORKFLOWS - Slow and not GC less
	 * 
	 * @return
	 */
	public String decodeOriginalId() {
		return decode(encoded, size).trim();
	}

	private static String decode(long[] encoded, int size) {
		if (size == 0) {
			return "";
		}

		StringBuilder idBuilder = new StringBuilder();
		for (int i = 0; i < size; i++) {
			long currEncodedValue = encoded[i];
			for (int j = 0; j < Long.BYTES; j++) {
				long mask = BYTE_MASK << (8 * j);
				long extracted = currEncodedValue & mask;
				extracted = extracted >> (8 * j);
				idBuilder.append((char) extracted);
			}
		}
		return idBuilder.toString();
	}

	@Override
	public String toString() {
		return decodeOriginalId();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		EfficientId that = (EfficientId) o;

		if (this.encoded == that.encoded)
			return true;

		if (that.size != size)
			return false;

		for (int i = 0; i < size; i++)
			if (this.encoded[i] != that.encoded[i])
				return false;

		return true;

	}

	@Override
	public int hashCode() {
		if (!isHashCodeCached) {
			cashedHashCode = hashCode(encoded, size);
			isHashCodeCached = true;
		}
		return cashedHashCode;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeByte(size);
		for (int i = 0; i < size; i++) {
			out.writeLong(encoded[i]);
		}

	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		this.isHashCodeCached = false;
		this.size = (int) in.readByte();
		if (this.size > encoded.length) {
			// unfortunately we have to resize;
			encoded = new long[this.size];
		}
		int index = 0;
		for (int i = 0; i < encoded.length; i++) {
			if (i < size) {
				encoded[i] = in.readLong();
			} else {
				encoded[i] = 0;
			}
		}
	}

	/**
	 * create several {@link EfficientId}s from string ids in one shot
	 * 
	 * @param ids
	 * @return
	 */
	public static EfficientId[] fromString(String[] ids) {
		EfficientId[] efficientIds = new EfficientId[ids.length];
		for (int i = 0; i < efficientIds.length; i++) {
			efficientIds[i] = new EfficientId(ids[i]);
		}
		return efficientIds;
	}

	public void internalize(EfficientId streamId) {

		isHashCodeCached = false;

		if (this.encoded.length < streamId.size) {
			this.encoded = new long[streamId.size];
		}
		this.size = streamId.size;
		for (int i = 0; i < size; i++) {
			this.encoded[i] = streamId.encoded[i];
		}
	}

	/**
	 * This is a copy of {@link Arrays#hashCode(long[])} method with additional agrument to calculate hash for
	 * only the first
	 * 
	 * @param length
	 *            elements
	 * 
	 * @param array
	 *            array to get hash code of
	 * @param length
	 *            the hash will be calculated for this number of first elements
	 * @return
	 */
	private static int hashCode(long[] array, int length) {
		if (array == null) {
			return 0;
		}

		int result = 1;
		for (int i = 0; i < length; i++) {
			long element = array[i];
			int elementHash = (int) (element ^ (element >>> 32));
			result = 31 * result + elementHash;
		}

		return result;
	}

	@Override
	public int compareTo(@NotNull EfficientId o) {
		// this can only work when the unused part of each long is defined !
		for (int i = 0; i < Math.min(size, o.size); i++) {
			final long result = encoded[i] - o.encoded[i];
			if (result != 0) {
				return result > 0 ? 1 : -1;
			}
		}
		if (size != o.size) {
			return size > o.size ? 1 : -1;
		}
		return 0;
	}
}
