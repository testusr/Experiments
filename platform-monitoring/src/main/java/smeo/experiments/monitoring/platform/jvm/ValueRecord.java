package smeo.experiments.monitoring.platform.jvm;

public class ValueRecord {
    long timestamp;
    ValueType valueType;

    double doubleValue;
    String stringValue;
    boolean booleanValue;
    long numberValue;

    public void update(ValueType valueType, Object value) {
        switch (valueType){
            case DOUBLE:
                doubleValue = (double) value;
                break;
            case STRING:
                stringValue = (String) value;
                break;
            case NUMBER:
                numberValue = Long.valueOf(String.valueOf(value));
                break;
            case BOOLEAN:
                booleanValue = (boolean) value;
                break;
            default:
                throw new IllegalArgumentException("unknown value type '"+valueType+"'");
        }
        this.valueType = valueType;
        timestamp = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        switch (valueType){
            case DOUBLE:
                return String.valueOf(doubleValue);
            case STRING:
                return stringValue;
            case NUMBER:
                return String.valueOf(numberValue);
            case BOOLEAN:
                return String.valueOf(booleanValue);
            default:
                return "unsupported value type '"+valueType+"'";
        }
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public enum ValueType {
        DOUBLE,
        STRING,
        NUMBER,
        BOOLEAN
    }
}
