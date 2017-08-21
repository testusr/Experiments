package smeo.experiments.simplefix.model;

import java.nio.ByteBuffer;

/**
 * Created by smeo on 20.08.17.
 */
public interface SimpleFixMessageValue {
    char ENTRY_SEPARATOR = 1;

    void clear();

    int tag();

    void write(ByteBuffer byteBuffer);

    int length();

    boolean hasValue();

    CharSequence stringValue();
}
