package smeo.experiments.simplefix.model;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by truehl on 18.08.17.
 */
public class SimpleFixMessage {
	public final byte TAG_SEPARATOR = 0x01;
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
	//36
	SimpleFixField msgSeqNum = new SimpleFixField();


	public void addValue(SimpleFixField value) {
		if (value.hasValue()) {
			SimpleFixField fixField = getFieldToAdd(value.tag());
			fixField.internalize(value);
		}
	}

	public SimpleFixField getFieldToAdd(int tag) {
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
				SimpleFixField nextFieldFromPool = nextFixFieldFromPool();
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

	public SimpleFixMessage addTag(int tag, String value) {
		final SimpleFixField simpleFixField = getFieldToAdd(tag);
		simpleFixField.setValue(tag, value);
		return this;
	}

	public SimpleFixMessage addTag(int tag, int value) {
		final SimpleFixField simpleFixField = getFieldToAdd(tag);
		simpleFixField.setValue(tag, value);
		return this;
	}

	public SimpleFixMessage addTag(int tag, char value) {
		final SimpleFixField simpleFixField = getFieldToAdd(tag);
		return this;
	}

	public SimpleFixMessage addTag(int tag, double value) {
		final SimpleFixField simpleFixField = getFieldToAdd(tag);
		simpleFixField.setValue(tag, value);
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
		write(messageType, byteBuffer, true);
		write(senderCompanyID, byteBuffer, true);
		write(senderSubID, byteBuffer, true);
		write(targetCompanyId, byteBuffer, true);
		write(targetSubId, byteBuffer, true);
		write(msgSeqNum, byteBuffer, true);

		for (int i = 0; i < messageValues.size(); i++) {
			write(messageValues.get(i), byteBuffer, true);
		}
		checkSum.setValue(FixTags.CheckSum.tag, calculateCheckSum(byteBuffer));
		write(checkSum, byteBuffer, true);

	}

	private int calculateCheckSum(ByteBuffer byteBuffer) {
		long checksum = 0;
		for (int i = 0; i <= byteBuffer.position(); i++) {
			checksum += (char) byteBuffer.get(i);
		}
		return (int) checksum % 256;
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
			final SimpleFixMessageValue simpleFixMessageValue = messageValues.get(i);
			totalLength += simpleFixMessageValue.length();
		}
		return totalLength;
	}

	private void write(SimpleFixMessageValue field, ByteBuffer bb, boolean addSeparator) {
		if (field.hasValue()) {
			final CharSequence charSequence = field.stringValue();
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

	public static String asString(SimpleFixMessage fixMessage) {
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
