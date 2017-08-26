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
    private StringBuffer fixString = new StringBuffer();

    public void clear() {
        type = null;
        fixString.setLength(0);
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
        fixString.setLength(0);

    }

    public void setValue(int id, int value) {
        this.tag = id;
        type = SimpleFixValueType.INTEGER;
        this.intValue = value;
        fixString.setLength(0);
    }

    public void setValue(int id, char value) {
        this.tag = id;
        type = SimpleFixValueType.CHAR;
        this.charValue = value;
        fixString.setLength(0);
    }

    public void setValue(int id, CharSequence value) {
        this.tag = id;
        type = SimpleFixValueType.STRING;
        stringValue.setLength(0);
        fixString.setLength(0);
        stringValue.append(value);
    }

    public CharSequence stringValue() {
        if (fixString.length() == 0) {
            fixString.append(tag).append('=');
            switch (type) {
                case INTEGER:
                    fixString.append(intValue);
                    break;
                case CHAR:
                    fixString.append(charValue);
                    break;
                case DOUBLE:
                    fixString.append(doubleValue);
                    break;
                case STRING:
                    fixString.append(stringValue);
                    break;
            }
        }
        return fixString;
    }

    @Override
    public CharSequence pureStringValue() {
        return stringValue;
    }

    public int length() {
        return stringValue().length();
    }

    public void internalize(SimpleFixField value) {
        clear();
        this.tag = value.tag;
        this.type = value.type;
        switch (value.type) {
            case STRING:
                this.stringValue.append(value.stringValue);
                break;
            case INTEGER:
                this.intValue = value.intValue;
                break;
            case CHAR:
                this.charValue = value.charValue;
                break;
            case DOUBLE:
                this.doubleValue = value.doubleValue;
                break;
        }
    }
}
