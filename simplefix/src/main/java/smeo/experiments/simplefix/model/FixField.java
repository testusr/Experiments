package smeo.experiments.simplefix.model;

/**
 * Reusable Fix Value Container.
 */
public class FixField implements FixMessageValue {
    private FixValueType type;
    private int tag;
    private double doubleValue;
    private int intValue;
    private char charValue;
    private StringBuffer stringValue = new StringBuffer();

    private StringBuffer fieldAsStringCache = new StringBuffer();

    public void clear() {
        type = null;
        fieldAsStringCache.setLength(0);
    }

    public int tag() {
        return tag;
    }

    public String toString() {
        return tagAndValue().toString();
    }

    public boolean hasValue() {
        return type != null;
    }

    public void setValue(int id, double value) {
        this.tag = id;
        type = FixValueType.DOUBLE;
        this.doubleValue = value;
        fieldAsStringCache.setLength(0);

    }

    public void setValue(int id, int value) {
        this.tag = id;
        type = FixValueType.INTEGER;
        this.intValue = value;
        fieldAsStringCache.setLength(0);
    }

    public void setValue(int id, char value) {
        this.tag = id;
        type = FixValueType.CHAR;
        this.charValue = value;
        fieldAsStringCache.setLength(0);
    }

    public void setValue(int id, CharSequence value) {
        this.tag = id;
        type = FixValueType.STRING;
        stringValue.setLength(0);
        fieldAsStringCache.setLength(0);
        stringValue.append(value);
    }

    public String valueAsString() {
        switch (type) {
            case INTEGER:
                return String.valueOf(intValue);
            case CHAR:
                return String.valueOf(charValue);
            case DOUBLE:
                return String.valueOf(doubleValue);
            case STRING:
                return stringValue.toString();
        }
        return null;
    }

    public StringBuffer tagAndValue() {
        if (fieldAsStringCache.length() == 0) {
            fieldAsStringCache.append(tag)
                    .append('=');
            switch (type) {
                case INTEGER:
                    fieldAsStringCache.append(intValue);
                    break;
                case CHAR:
                    fieldAsStringCache.append(charValue);
                    break;
                case DOUBLE:
                    fieldAsStringCache.append(doubleValue);
                    break;
                case STRING:
                    fieldAsStringCache.append(stringValue);
                    break;
            }
        }
        return fieldAsStringCache;
    }

    @Override
    public CharSequence pureStringValue() {
        return stringValue;
    }

    /**
     * @return the length of the element in the message including the separator
     */
    public int length() {
        if (hasValue()) {
            return tagAndValue().length() + 1;
        }
        return 0;
    }

    public void internalize(FixField value) {
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
