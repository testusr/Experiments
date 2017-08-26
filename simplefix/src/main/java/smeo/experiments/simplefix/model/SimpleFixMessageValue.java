package smeo.experiments.simplefix.model;

/**
 * Created by smeo on 20.08.17.
 */
public interface SimpleFixMessageValue {
    char ENTRY_SEPARATOR = 1;

    void clear();

    int tag();

    int length();

    boolean hasValue();

    CharSequence stringValue();

    CharSequence pureStringValue();
}
