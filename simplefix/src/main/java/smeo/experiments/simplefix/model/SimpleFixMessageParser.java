package smeo.experiments.simplefix.model;

import java.nio.ByteBuffer;

/**
 * Created by truehl on 21.08.17.
 */
public class SimpleFixMessageParser {
	private static final int CHECK_SUM_TAG = 10;
	private static final int BEGIN_STRING_TAG = 8;

	SimpleFixField tempFixField = new SimpleFixField();
	StringBuffer tempBuffer = new StringBuffer();


	public SimpleFixField parseNextFixField(ByteBuffer srcByteBuffer) {
		parseNextFixField(srcByteBuffer, tempFixField);
		return tempFixField;
	}

	public void parseNextFixField(ByteBuffer srcByteBuffer, SimpleFixField targetField) {
		tempBuffer.setLength(0);
		boolean equalSignFound = false;
		int tagId = 0;
		tempFixField.clear();
		int pos_before_message = srcByteBuffer.position();
		while (srcByteBuffer.hasRemaining()) {
			char currChar = (char) srcByteBuffer.get();
			switch (currChar) {
				case '=': {
					tagId = Integer.valueOf(tempBuffer.toString());
					equalSignFound = true;
					tempBuffer.setLength(0);
				}
				break;
				case SimpleFixMessageValue.ENTRY_SEPARATOR: {
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


	public ParseResult parseNextMessage(ByteBuffer srcByteBuffer, SimpleFixMessage targetMessage) {
		while (srcByteBuffer.hasRemaining()) {
			parseNextFixField(srcByteBuffer, tempFixField);
			targetMessage.addValue(tempFixField);
			if (isCheckSum(tempFixField)) {
				return ParseResult.MSG_COMPLETE;
			}
		}
		return ParseResult.MSG_INCOMPLETE;
	}

	private boolean isCheckSum(SimpleFixField simpleFixField) {
		return simpleFixField.tag() == CHECK_SUM_TAG;
	}

	public enum ParseResult {
		MSG_COMPLETE, MSG_INCOMPLETE, INVALID_MESSAGE, NO_DATA;
	}
}
