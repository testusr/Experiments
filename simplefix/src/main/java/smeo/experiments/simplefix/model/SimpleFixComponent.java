package smeo.experiments.simplefix.model;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Created by smeo on 19.08.17.
 */
public class SimpleFixComponent implements SimpleFixMessageValue {
    int tag;
    boolean hasValue = true;
    List<SimpleFixMessageValue> values;

    private SimpleFixComponent() {
    }

    @Override
    public void clear() {
        hasValue = false;
        for (int i = 0; i < values.size(); i++) {
            values.get(i).clear();
        }
    }

    public boolean hasValue() {
        return hasValue;
    }

    @Override
    public CharSequence stringValue() {
        return null;
    }

    @Override
    public int tag() {
        return tag;
    }

    @Override
    public void write(ByteBuffer byteBuffer) {

    }

    @Override
    public int length() {
        return 0;
    }
}
