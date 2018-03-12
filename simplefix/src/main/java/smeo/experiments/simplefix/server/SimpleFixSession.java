package smeo.experiments.simplefix.server;

import smeo.experiments.simplefix.model.FixField;
import smeo.experiments.simplefix.model.FixMessage;
import smeo.experiments.simplefix.model.SimpleFixMessageParser;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class SimpleFixSession {
    SimpleSessionConfig sessionConfig;
    SessionState sessionState;
    private boolean isConnected = false;

    private ByteBuffer writeBuffer = ByteBuffer.allocateDirect(2096);
    private SocketChannel socketChannel;

    private ByteBuffer readBuffer = ByteBuffer.allocateDirect(2096);
    private SimpleFixMessageParser incomingMessageParser = new SimpleFixMessageParser();
    private FixMessage incomingFixMessage = new FixMessage();
    private FixMessage controlMessagePreallocated = new FixMessage();

    public SimpleFixSession(SimpleSessionConfig sessionConfig) {
        this.sessionConfig = sessionConfig;
        initSession();
    }

    private void initSession() {
        this.sessionState = new SessionState();
    }


    private void resetSequence() {
        System.out.println("[SESSION '" + sessionConfig.toString() + " resetting sequence']");
        controlMessagePreallocated.refurbish();
        controlMessagePreallocated.addTag(35, 4); // SequenceReset
        controlMessagePreallocated.addTag(36, sessionState.seqNo + 1); // NewSeqNo
        sendControlMessage(controlMessagePreallocated);
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
        this.sessionState.seqNo = 1;
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

    public void sendControlMessage(FixMessage fixMessage) {
        sendMessage(fixMessage);
        System.out.println("[SESSION '" + sessionConfig.toString() + "'] send message '" + FixMessage.asString(fixMessage) + "'");//:  '" + FixMessage.asString(fixMessage));
    }

    public void sendSessionMessage(FixMessage fixMessage) {
        fixMessage.beginString(sessionConfig.beginString());

        fixMessage.senderCompanyID(sessionConfig.targetCompID);
        fixMessage.senderSubID(sessionConfig.targetSubID);
        fixMessage.targetCompanyId(sessionConfig.senderCompID);
        fixMessage.targetSubId(sessionConfig.senderSubID);

        fixMessage.msgSeqNum(sessionState.nextSeqId());
        writeBuffer.clear();
        fixMessage.writeToByteBuffer(writeBuffer);
//        System.out.println("[SESSION '" + sessionConfig.toString() + "'] --> from Buffer: " + new String(writeBuffer.array(), Charset.forName("UTF-8")));
        System.out.println("[SESSION '" + sessionConfig.toString() + "'] --> from FixMessage: " + FixMessage.asString(fixMessage));


        try {
            writeBuffer.flip();
            while (writeBuffer.hasRemaining()) {
                socketChannel.write(writeBuffer);
            }
            writeBuffer.clear();
        } catch (IOException e) {
            System.out.println("[SESSION '" + sessionConfig.toString() + "'] caught IOException, disconnecting '" + e.getMessage() + "'");
            this.isConnected = false;
            e.printStackTrace();
        } catch (Exception e) {
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
                readBuffer.flip();
                if (incomingMessageParser.parseNextMessage(readBuffer, incomingFixMessage) == SimpleFixMessageParser.ParseResult.MSG_COMPLETE) {
                    readBuffer.compact();
                    processIncomingMessage(sessionConfig, incomingFixMessage);
                }
                readBuffer.flip();
            }
        }
    }

    private void processIncomingMessage(SimpleSessionConfig sessionConfig, FixMessage incomingFixMessage) {
        System.out.println("[SESSION '" + sessionConfig.toString() + "'] incoming message: " + FixMessage.asString(incomingFixMessage));
        switch (incomingFixMessage.messageType().toString()) {
            case "2": // resend message - we dont we reset
                resetSequence();
                break;
            case "1": // test request - answer with heartbeat
                processTestRequest(incomingFixMessage);
                break;
            default:
        }
    }

    private void processTestRequest(FixMessage incomingFixMessage) {
        controlMessagePreallocated.refurbish();
        controlMessagePreallocated.addTag(35, 0); // heartbeat
        final FixField preallocatedFieldForTag = incomingFixMessage.getPreallocatedFieldForTag(112);
        final String receivedTestRequestId = preallocatedFieldForTag.valueAsString();
        controlMessagePreallocated.addTag(112, receivedTestRequestId); // received test req id
        sendControlMessage(controlMessagePreallocated);
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
