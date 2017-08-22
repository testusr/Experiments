package smeo.experiments.simplefix.model;

/**
 * Created by smeo on 19.08.17.
 */
public class SimpleFixField implements SimpleFixMessageValue {
	private SimpleFixValueType type;
	private int tag;
	private double doubleValue;
	private int intValue;
	private char charValue;
	private StringBuffer stringValue = new StringBuffer();

	public void clear() {
		type = null;
		stringValue.setLength(0);
	}

	public int tag() {
		return tag;
	}

	public String toString() {
		return stringValue().toString();
	}

	public boolean hasValue() {
		return type != null;
	}

	public void setValue(int id, double value) {
		this.tag = id;
		type = SimpleFixValueType.DOUBLE;
		this.doubleValue = value;
	}

	public void setValue(int id, int value) {
		this.tag = id;
		type = SimpleFixValueType.INTEGER;
		this.intValue = value;
		stringValue.setLength(0);
	}

	public void setValue(int id, char value) {
		this.tag = id;
		type = SimpleFixValueType.CHAR;
		this.charValue = value;
		stringValue.setLength(0);
	}

	public void setValue(int id, CharSequence value) {
		this.tag = id;
		type = SimpleFixValueType.STRING;
		stringValue.setLength(0);
		stringValue.append(tag).append('=').append(value);
	}

	public CharSequence stringValue() {
		if (stringValue.length() == 0) {
			stringValue.append(tag).append('=');
			switch (type) {
			case INTEGER:
				stringValue.append(intValue);
				break;
			case CHAR:
				stringValue.append(charValue);
				break;
			case DOUBLE:
				stringValue.append(doubleValue);
				break;
			}
		}
		return stringValue;
	}

	public int length() {
		return stringValue().length();
	}

}
