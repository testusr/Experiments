package smeo.experiments.simplefix.model;

import java.nio.ByteBuffer;

/**
 * Created by truehl on 21.08.17.
 */
public class SimpleFixMessageParser {
    SimpleFixField tempFixField = new SimpleFixField();
    StringBuffer tempBuffer = new StringBuffer();

    public void parse(SimpleFixMessage targetMessage, ByteBuffer srcByteBuffer) {
        SimpleFixField fixField = parseNextFixField(srcByteBuffer, tempFixField);
    }

    public void parseNextFixField(ByteBuffer srcByteBuffer, SimpleFixMessage simpleFixMessage) {
        while (srcByteBuffer.hasRemaining()) {
            System.out.println(parseNextFixField(srcByteBuffer, simpleFixMessage.nextFixFieldFromPool()));
        }
    }

    public SimpleFixField parseNextFixField(ByteBuffer srcByteBuffer) {
        parseNextFixField(srcByteBuffer, tempFixField);
        return tempFixField;
    }

    public SimpleFixField parseNextFixField(ByteBuffer srcByteBuffer, SimpleFixField targetField) {
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
                    updateValue(tagId, tempBuffer);
                    return tempFixField;
                }
                default:
                    tempBuffer.append(currChar);
            }

        }
        return null;
    }

    private void updateValue(int tagId, StringBuffer tempBuffer) {
        tempFixField.setValue(tagId, tempBuffer);
    }
}
