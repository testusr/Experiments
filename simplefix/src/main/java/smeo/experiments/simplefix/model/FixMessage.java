package smeo.experiments.simplefix.model;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by truehl on 18.08.17.
 */
public class FixMessage {
    public final byte TAG_SEPARATOR = 0x01;
    public final byte EQUALS = '=';
    List<FixMessageValue> messageValues = new ArrayList<>();
    List<FixField> fieldPool = new ArrayList<>();

    // 8
    FixField beginString = new FixField();
    // 9
    FixField bodyLength = new FixField();
    // 35
    FixField messageType = new FixField();
    // 49
    FixField senderCompanyID = new FixField();
    // 50
    FixField senderSubID = new FixField();
    //56
    FixField targetCompanyId = new FixField();
    //57
    FixField targetSubId = new FixField();
    //10
    FixField checkSum = new FixField();
    //36
    FixField msgSeqNum = new FixField();


    public void addValue(FixField value) {
        if (value.hasValue()) {
            FixField fixField = getPreallocatedFieldForTag(value.tag());
            fixField.internalize(value);
        }
    }

    public FixField getPreallocatedFieldForTag(int tag) {
        switch (tag) {
            case 8: {
                return beginString;
            }
            case 9: {
                return bodyLength;
            }
            case 35: {
                return messageType;
            }
            case 34: {
                return msgSeqNum;
            }
            case 49: {
                return senderCompanyID;
            }
            case 50: {
                return senderSubID;
            }
            case 56: {
                return targetCompanyId;
            }
            case 57: {
                return targetSubId;
            }
            case 10: {
                return checkSum;
            }
            default: {
                FixField nextFieldFromPool = nextFixFieldFromPool();
                messageValues.add(nextFieldFromPool);
                return nextFieldFromPool;
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

    public FixMessage addTag(int tag, String value) {
        final FixField simpleFixField = getPreallocatedFieldForTag(tag);
        simpleFixField.setValue(tag, value);
        return this;
    }

    public FixMessage addTag(int tag, int value) {
        final FixField simpleFixField = getPreallocatedFieldForTag(tag);
        simpleFixField.setValue(tag, value);
        return this;
    }

    public FixMessage addTag(int tag, char value) {
        final FixField simpleFixField = getPreallocatedFieldForTag(tag);
        return this;
    }

    public FixMessage addTag(int tag, double value) {
        final FixField simpleFixField = getPreallocatedFieldForTag(tag);
        simpleFixField.setValue(tag, value);
        return this;
    }

    FixField nextFixFieldFromPool() {
        for (int i = 0; i < fieldPool.size(); i++) {
            final FixField currField = fieldPool.get(i);
            if (!currField.hasValue()) {
                return currField;
            }
        }
        final FixField newFixField = new FixField();
        fieldPool.add(newFixField);
        return newFixField;
    }

    public void writeToByteBuffer(ByteBuffer byteBuffer) {
        int startPosition = byteBuffer.position();
        write(beginString, byteBuffer, true);
        bodyLength.setValue(FixTags.BodyLength.tag, valueBodyLength());
        int startBody = byteBuffer.position();
        write(bodyLength, byteBuffer, true);
        write(messageType, byteBuffer, true);
        write(senderCompanyID, byteBuffer, true);
        write(senderSubID, byteBuffer, true);
        write(targetCompanyId, byteBuffer, true);
        write(targetSubId, byteBuffer, true);
        write(msgSeqNum, byteBuffer, true);

        for (int i = 0; i < messageValues.size(); i++) {
            write(messageValues.get(i), byteBuffer, true);
        }
        System.out.println("bb: " + new String(byteBuffer.array(), Charset.forName("UTF-8")));
        checkSum.setValue(FixTags.CheckSum.tag, calculateCheckSum(startPosition, byteBuffer));
        write(checkSum, byteBuffer, true);

    }

    private int calculateCheckSum(int startPosition, ByteBuffer byteBuffer) {
        StringBuffer stringBuffer = new StringBuffer();
        long sum = 0;
        for (int i = 0; i < byteBuffer.position(); i++) {
            sum += (char) byteBuffer.get(startPosition + i);
            stringBuffer.append((char) byteBuffer.get(startPosition + i));
        }
        final int checksum = (int) sum % 256;

        System.out.println("checkSumOn: '" + stringBuffer.toString() + "' \n cs: '" + checksum + "'");
        return checksum;
    }

    private int valueBodyLength() {
        int totalLength = 0;
        totalLength += messageType.length();
        totalLength += senderCompanyID.length();
        totalLength += senderSubID.length();
        totalLength += targetCompanyId.length();
        totalLength += targetSubId.length();
        totalLength += msgSeqNum.length();

        for (int i = 0; i < messageValues.size(); i++) {
            final FixMessageValue simpleFixMessageValue = messageValues.get(i);
            totalLength += simpleFixMessageValue.length();
        }
        return totalLength;
    }

    private void write(FixMessageValue field, ByteBuffer bb, boolean addSeparator) {
        if (field.hasValue()) {
            final CharSequence charSequence = field.tagAndValue();
            for (int i = 0; i < charSequence.length(); i++) {
                bb.put((byte) charSequence.charAt(i));
            }
            bb.put(TAG_SEPARATOR);
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
        msgSeqNum.clear();
        checkSum.clear();
        for (int i = 0; i < this.messageValues.size(); i++) {
            messageValues.get(i)
                    .clear();
        }
        messageValues.clear();

    }

    public static String asString(FixMessage fixMessage) {
        final ByteBuffer byteBuffer = ByteBuffer.allocate(2098);
        fixMessage.writeToByteBuffer(byteBuffer);
        byteBuffer.flip();
        StringBuffer stringBuffer = new StringBuffer();
        while (byteBuffer.hasRemaining()) {
            stringBuffer.append((char) byteBuffer.get());
        }
        return stringBuffer.toString();
    }


    public void msgSeqNum(int i) {
        this.msgSeqNum.setValue(34, i);
    }
}
