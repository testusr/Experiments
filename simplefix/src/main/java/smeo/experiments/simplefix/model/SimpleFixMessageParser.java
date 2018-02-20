package smeo.experiments.simplefix.model;

import java.nio.ByteBuffer;

/**
 * Created by truehl on 21.08.17.
 */
public class SimpleFixMessageParser {
	private static final int CHECK_SUM_TAG = 10;
	private static final int BEGIN_STRING_TAG = 8;

	FixField tempFixField = new FixField();
	StringBuffer tempBuffer = new StringBuffer();


	public FixField parseNextFixField(ByteBuffer srcByteBuffer) {
		parseNextFixField(srcByteBuffer, tempFixField);
		return tempFixField;
	}

	public void parseNextFixField(ByteBuffer srcByteBuffer, FixField targetField) {
		tempBuffer.setLength(0);
		int tagId = 0;
		tempFixField.clear();
		int pos_before_message = srcByteBuffer.position();
		while (srcByteBuffer.hasRemaining()) {
			char currChar = (char) srcByteBuffer.get();
			switch (currChar) {
				case '=': {
					tagId = Integer.valueOf(tempBuffer.toString());
					tempBuffer.setLength(0);
				}
				break;
				case FixMessageValue.ENTRY_SEPARATOR: {
					targetField.setValue(tagId, tempBuffer);
					return;
				}
				default:
					tempBuffer.append(currChar);
			}

		}
		// could not be fully read, start again next time.
		srcByteBuffer.position(pos_before_message);
	}


	public ParseResult parseNextMessage(ByteBuffer srcByteBuffer, FixMessage targetMessage) {
		while (srcByteBuffer.hasRemaining()) {
			parseNextFixField(srcByteBuffer, tempFixField);
			targetMessage.addValue(tempFixField);
			if (isCheckSum(tempFixField)) {
				return ParseResult.MSG_COMPLETE;
			}
		}
		return ParseResult.MSG_INCOMPLETE;
	}

	public static void parseFromFixString(String fixMessage, String separator, FixMessage targetMessage) {
		String[] splitElements = fixMessage.split(separator);
		for (int i = 0; i < splitElements.length; i++) {
			String keyValueSplit[] = splitElements[i].split("=");
			targetMessage.addTag(Integer.valueOf(keyValueSplit[0]), keyValueSplit[1]);
		}
	}

	private boolean isCheckSum(FixField simpleFixField) {
		return simpleFixField.tag() == CHECK_SUM_TAG;
	}

	public enum ParseResult {
		MSG_COMPLETE, MSG_INCOMPLETE, INVALID_MESSAGE, NO_DATA;
	}
}
