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
    List<SimpleFixComponent> componentPool;

    SimpleFixField beginString = new SimpleFixField();
    SimpleFixField bodyLength = new SimpleFixField();
    SimpleFixField messageType = new SimpleFixField();
    SimpleFixField senderCompanyID = new SimpleFixField();
    SimpleFixField senderSubID = new SimpleFixField();
    SimpleFixField targetCompanyId = new SimpleFixField();
    SimpleFixField targetSubId = new SimpleFixField();
    SimpleFixField checkSum = new SimpleFixField();


    public void beginString(CharSequence value) {
        beginString.setValue(FixTags.BeginString.tag, value);
    }

    public void messageType(CharSequence value) {
        messageType.setValue(FixTags.MsgType.tag, value);
    }

    public void senderCompanyID(CharSequence value) {
        senderCompanyID.setValue(FixTags.SenderCompID.tag, value);
    }

    public void senderSubID(CharSequence value) {
        senderSubID.setValue(FixTags.SenderSubID.tag, value);
    }

    public void targetCompanyId(CharSequence value) {
        targetCompanyId.setValue(FixTags.TargetCompID.tag, value);
    }

    public void targetSubId(CharSequence value) {
        targetSubId.setValue(FixTags.TargetSubID.tag, value);
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
}
