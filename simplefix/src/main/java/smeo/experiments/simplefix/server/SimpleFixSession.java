package smeo.experiments.simplefix.server;

import smeo.experiments.simplefix.model.FixMessage;
import smeo.experiments.simplefix.model.SimpleFixMessageParser;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

public class SimpleFixSession {
    SimpleSessionConfig sessionConfig;
    SessionContext sessionContext;
    private boolean isConnected = false;

    private ByteBuffer writeBuffer = ByteBuffer.allocate(2096);
    private SocketChannel socketChannel;

    private ByteBuffer readBuffer = ByteBuffer.allocate(2096);
    private SimpleFixMessageParser incomingMessageParser = new SimpleFixMessageParser();
    private FixMessage incomingFixMessage = new FixMessage();

    public SimpleFixSession(SimpleSessionConfig sessionConfig) {
        this.sessionConfig = sessionConfig;
        initSession();
    }

    private void initSession() {
        this.sessionContext = new SessionContext();
    }


    public boolean isConnected() {
        return this.isConnected && socketChannel != null && socketChannel.isConnected();
    }

    public void linkToSocketChannel(SocketChannel socketChannel) throws IOException {
        if (this.socketChannel != null) {
            try {
                this.socketChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.isConnected = false;
        this.socketChannel = socketChannel;
        this.socketChannel.configureBlocking(false);
        this.sessionContext.seqNo = 1;
    }

    public void markAsConnected() {
        this.isConnected = true;
    }

    public boolean sendMessage(FixMessage fixMessage) {
        if (isConnected()) {
            sendSessionMessage(fixMessage);
            return true;
        }
        return false;
    }

    public void sendSessionMessage(FixMessage fixMessage) {
        fixMessage.beginString(sessionConfig.beginString());

        fixMessage.senderCompanyID(sessionConfig.targetCompID);
        fixMessage.senderSubID(sessionConfig.targetSubID);
        fixMessage.targetCompanyId(sessionConfig.senderCompID);
        fixMessage.targetSubId(sessionConfig.senderSubID);

        fixMessage.msgSeqNum(sessionContext.nextSeqId());
        writeBuffer.clear();
        fixMessage.writeToByteBuffer(writeBuffer);

        System.out.println("'send:\n'" + new String(writeBuffer.array(), Charset.forName("UTF-8")));
        try {
            writeBuffer.flip();
            while (writeBuffer.hasRemaining()) {
                socketChannel.write(writeBuffer);
            }
            writeBuffer.compact();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void readMessage() {
        if (isConnected) {
            boolean dataRead = false;
            try {
                while (socketChannel.read(readBuffer) > 0) {
                    dataRead = true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (dataRead) {
                if (incomingMessageParser.parseNextMessage(readBuffer, incomingFixMessage) == SimpleFixMessageParser.ParseResult.MSG_COMPLETE) {
                    readBuffer.compact();
                    processIncomingMessage(sessionConfig, incomingFixMessage);
                }
            }
        }
    }

    private void processIncomingMessage(SimpleSessionConfig sessionConfig, FixMessage incomingFixMessage) {
        System.out.println("incoming message:  '" + sessionConfig.toString() + "': " + FixMessage.asString(incomingFixMessage));
    }


    public boolean matches(CharSequence senderCompanyId, CharSequence senderSubId, CharSequence targetCompanyId, CharSequence targetSubId) {
        return equals(targetCompanyId, sessionConfig.targetCompID()) && equals(senderSubId, sessionConfig.senderSubID()) && equals(senderCompanyId, sessionConfig.senderCompID()) && equals(senderSubId, sessionConfig.senderSubID());
    }

    private boolean equals(CharSequence charSequence, String string) {
        if (charSequence.length() != string.length()) return false;
        for (int i = 0; i < charSequence.length(); i++) {
            if (charSequence.charAt(i) != string.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return SimpleSessionConfig.sessionId(senderCompId(), senderSubId(), targetCompId(), targetSubId());
    }


    public String targetSubId() {
        return sessionConfig.targetSubID();
    }

    public String targetCompId() {
        return sessionConfig.targetCompID();
    }

    public String senderSubId() {
        return sessionConfig.senderSubID();
    }

    public String senderCompId() {
        return sessionConfig.senderCompID();
    }

}
