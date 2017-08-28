package smeo.experiments.simplefix.model;

/**
 * Created by smeo on 20.08.17.
 */
public interface SimpleFixMessageValue {
	byte ENTRY_SEPARATOR = 0x01;

	void clear();

	int tag();

	int length();

	boolean hasValue();

	CharSequence stringValue();

	CharSequence pureStringValue();
}
