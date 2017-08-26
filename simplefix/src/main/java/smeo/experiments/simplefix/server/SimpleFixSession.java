package smeo.experiments.simplefix.server;

import smeo.experiments.simplefix.model.SimpleFixMessage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class SimpleFixSession {
    SimpleSessionConfig sessionConfig;
    SessionContext sessionContext;
    private ByteBuffer byteBuffer = ByteBuffer.allocate(2096);
    private SocketChannel socketChannel;
    private String targetIpAdress = "";

    public SimpleFixSession(SimpleSessionConfig sessionConfig) {
        this.sessionConfig = sessionConfig;
        initSession(sessionConfig);
    }

    private void initSession(SimpleSessionConfig sessionConfig) {
        this.sessionContext = new SessionContext();
    }


    public boolean isConnected() {
        return socketChannel != null && socketChannel.isConnected();
    }

    public void linkToSocketChannel(SocketChannel socketChannel) {
        if (this.socketChannel != null) {
            try {
                this.socketChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.socketChannel = socketChannel;
    }

    public void sendMessage(SimpleFixMessage fixMessage) {
        if (isConnected()) {
            fixMessage.beginString(sessionConfig.beginString());
            //fixMessage.senderSeqNo(sessionContext.nextSeqId());
            fixMessage.writeToByteBuffer(byteBuffer);
            try {
                byteBuffer.flip();
                while (byteBuffer.hasRemaining()) {
                    socketChannel.write(byteBuffer);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean matches(CharSequence senderCompanyId, CharSequence senderSubId, CharSequence targetCompanyId, CharSequence targetSubId) {
        return equals(targetCompanyId, sessionConfig.targetCompID()) && equals(targetSubId, sessionConfig.senderSubID())
                && equals(senderCompanyId, sessionConfig.targetCompID()) && equals(senderSubId, sessionConfig.targetSubID());
    }

    private boolean equals(CharSequence charSequence, String string) {
        if (charSequence.length() != string.length())
            return false;
        for (int i = 0; i < charSequence.length(); i++) {
            if (charSequence.charAt(i) != string.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    public int id() {
        return 0;
    }

    public long connectionTimestamp() {
        return 0;
    }

    public String targetIpAdress() {
        return targetIpAdress;
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

    public String senderCompIp() {
        return sessionConfig.senderCompID();
    }
}
