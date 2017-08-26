
package smeo.experiments.simplefix.model;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by truehl on 18.08.17.
 */
public class SimpleFixMessage {
    public final byte TAG_SEPARATOR = 1;
    public final byte EQUALS = '=';
    List<SimpleFixMessageValue> messageValues = new ArrayList<>();
    List<SimpleFixField> fieldPool = new ArrayList<>();

    // 8
    SimpleFixField beginString = new SimpleFixField();
    // 9
    SimpleFixField bodyLength = new SimpleFixField();
    // 35
    SimpleFixField messageType = new SimpleFixField();
    // 49
    SimpleFixField senderCompanyID = new SimpleFixField();
    // 50
    SimpleFixField senderSubID = new SimpleFixField();
    //56
    SimpleFixField targetCompanyId = new SimpleFixField();
    //57
    SimpleFixField targetSubId = new SimpleFixField();
    //10
    SimpleFixField checkSum = new SimpleFixField();

    public void addValue(SimpleFixField value) {
        switch (value.tag()) {
            case 8: {
                beginString.internalize(value);
                break;
            }
            case 9: {
                bodyLength.internalize(value);
                break;
            }
            case 35: {
                messageType.internalize(value);
                break;
            }
            case 49: {
                senderCompanyID.internalize(value);
                break;
            }
            case 50: {
                senderSubID.internalize(value);
                break;
            }
            case 56: {
                targetCompanyId.internalize(value);
                break;
            }
            case 57: {
                targetSubId.internalize(value);
                break;
            }
            case 10: {
                checkSum.internalize(value);
                break;
            }
            default: {
                final SimpleFixField simpleFixField = nextFixFieldFromPool();
                simpleFixField.internalize(value);
                messageValues.add(simpleFixField);
            }
        }
    }

    public void beginString(CharSequence value) {
        beginString.setValue(FixTags.BeginString.tag, value);
    }

    public void messageType(CharSequence value) {
        messageType.setValue(FixTags.MsgType.tag, value);
    }

    public CharSequence messageType() {
        if (messageType.hasValue()) {
            return messageType.pureStringValue();
        }
        return null;
    }

    public void senderCompanyID(CharSequence value) {
        senderCompanyID.setValue(FixTags.SenderCompID.tag, value);
    }

    public CharSequence senderCompanyID() {
        if (senderCompanyID.hasValue()) {
            return senderCompanyID.pureStringValue();
        }
        return null;
    }

    public void senderSubID(CharSequence value) {
        senderSubID.setValue(FixTags.SenderSubID.tag, value);
    }

    public CharSequence senderSubID() {
        if (senderSubID.hasValue()) {
            return senderSubID.pureStringValue();
        }
        return null;
    }

    public void targetCompanyId(CharSequence value) {
        targetCompanyId.setValue(FixTags.TargetCompID.tag, value);
    }

    public CharSequence targetCompanyId() {
        if (targetCompanyId.hasValue()) {
            return targetCompanyId.pureStringValue();
        }
        return null;
    }

    public void targetSubId(CharSequence value) {
        targetSubId.setValue(FixTags.TargetSubID.tag, value);
    }

    public CharSequence targetSubId() {
        if (targetSubId.hasValue()) {
            return targetSubId.pureStringValue();
        }
        return null;
    }

    public SimpleFixMessage addTag(int tag, String value) {
        final SimpleFixField simpleFixField = nextFixFieldFromPool();
        simpleFixField.setValue(tag, value);
        messageValues.add(simpleFixField);
        return this;
    }

    public SimpleFixMessage addTag(int tag, int value) {
        final SimpleFixField simpleFixField = nextFixFieldFromPool();
        simpleFixField.setValue(tag, value);
        messageValues.add(simpleFixField);
        return this;
    }

    public SimpleFixMessage addTag(int tag, char value) {
        final SimpleFixField simpleFixField = nextFixFieldFromPool();
        simpleFixField.setValue(tag, value);
        messageValues.add(simpleFixField);
        return this;
    }

    public SimpleFixMessage addTag(int tag, double value) {
        final SimpleFixField simpleFixField = nextFixFieldFromPool();
        simpleFixField.setValue(tag, value);
        messageValues.add(simpleFixField);
        return this;
    }

    SimpleFixField nextFixFieldFromPool() {
        for (int i = 0; i < fieldPool.size(); i++) {
            final SimpleFixField currField = fieldPool.get(i);
            if (!currField.hasValue()) {
                return currField;
            }
        }
        final SimpleFixField newFixField = new SimpleFixField();
        fieldPool.add(newFixField);
        return newFixField;
    }

    public void writeToByteBuffer(ByteBuffer byteBuffer) {
        write(beginString, byteBuffer, true);
        bodyLength.setValue(FixTags.BodyLength.tag, valueBodyLength());
        int startBody = byteBuffer.position();
        write(bodyLength, byteBuffer, true);
        for (int i = 0; i < messageValues.size(); i++) {
            write(messageValues.get(i), byteBuffer, true);
        }
        checkSum.setValue(FixTags.CheckSum.tag, calculateCheckSum(startBody, byteBuffer));
        write(checkSum, byteBuffer, false);

    }

    private int calculateCheckSum(int startBody, ByteBuffer byteBuffer) {
        long checksum = 0;
        for (int i = startBody; i < byteBuffer.position(); i++) {
            checksum += byteBuffer.getChar(i);
        }
        return (int) checksum % 256;
    }

    private int valueBodyLength() {
        int totalLength = 0;

        for (int i = 0; i < messageValues.size(); i++) {
            final SimpleFixMessageValue simpleFixMessageValue = messageValues.get(i);
            totalLength += simpleFixMessageValue.length();
            if (simpleFixMessageValue.hasValue()) {
                totalLength++; // separator char between fix values
            }
        }
        return totalLength;
    }

    private void write(SimpleFixMessageValue field, ByteBuffer bb, boolean addSeparator) {
        if (field.hasValue()) {
            final CharSequence charSequence = field.stringValue();
            for (int i = 0; i < charSequence.length(); i++) {
                bb.putChar(charSequence.charAt(i));
            }
            bb.putChar(SimpleFixMessageValue.ENTRY_SEPARATOR);
        }
    }

    public void refurbish() {
        beginString.clear();
        bodyLength.clear();
        messageType.clear();
        senderCompanyID.clear();
        senderSubID.clear();
        targetCompanyId.clear();
        targetSubId.clear();
        checkSum.clear();
        for (int i = 0; i < this.messageValues.size(); i++) {
            messageValues.get(i).clear();
        }
        messageValues.clear();

    }


}
