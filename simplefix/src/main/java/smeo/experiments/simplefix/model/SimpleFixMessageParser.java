package smeo.experiments.simplefix.model;

import java.nio.ByteBuffer;

/**
 * Created by truehl on 21.08.17.
 */
public class SimpleFixMessageParser {
	SimpleFixField tempFixField = new SimpleFixField();
	StringBuffer tempBuffer = new StringBuffer();

	public void parse(SimpleFixMessage targetMessage, ByteBuffer srcByteBuffer) {
		SimpleFixField fixField = parseNextFixField(srcByteBuffer);
	}

	public SimpleFixField parseNextFixField(ByteBuffer srcByteBuffer) {
		tempBuffer.setLength(0);
		boolean equalSignFound = false;
		int tagId = 0;
		tempFixField.clear();
		int position = srcByteBuffer.position();
		while (srcByteBuffer.hasRemaining()) {
			char currChar = srcByteBuffer.getChar();
			switch (currChar) {
			case '=': {
				tagId = Integer.valueOf(tempBuffer.toString());
				equalSignFound = true;
				tempBuffer.setLength(0);
			}
				break;
			case SimpleFixMessageValue.ENTRY_SEPARATOR: {
				tempFixField.setValue(tagId, tempBuffer);
				return tempFixField;
			}
			default:
				tempBuffer.append(currChar);
			}

		}
		return null;
	}
}
