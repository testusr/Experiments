package smeo.experiments.simplefix.server;

import smeo.experiments.simplefix.model.FixMessageType;
import smeo.experiments.simplefix.model.FixMessage;
import smeo.experiments.simplefix.model.SimpleFixMessageParser;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Listening to incoming session requests
 */
public class SimpleFixServer {
    final int port;
    final String address;
    private AtomicBoolean isListening = new AtomicBoolean(false);
    private Thread listenerThread;
    private ByteBuffer buf = ByteBuffer.allocate(4096);
    private FixMessage incomingMessage = new FixMessage();
    private SimpleFixMessageParser fixMessageParser = new SimpleFixMessageParser();
    private List<SimpleFixSession> fixSessions = new ArrayList<>();

    public SimpleFixServer(int port, String address) {
        this.port = port;
        this.address = address;
    }

    public SimpleFixSession initFixClientSession(SimpleSessionConfig sessionConfig) {
        System.out.println("setup session: " + sessionConfig);
        SimpleFixSession newSession = new SimpleFixSession(sessionConfig);
        fixSessions.add(newSession);
        return newSession;
    }


    public void startListening() {
        if (listenerThread == null) {
            this.listenerThread = new Thread(() -> startListening(port));
            listenerThread.start();
        }

    }

    public void startListening(int port) {
        if (isListening.getAndSet(true)) {
            throw new IllegalArgumentException("listening thread already running");
        }

        try {
            // server socket in blocking mode
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket()
                    .bind(new InetSocketAddress(port));
            System.out.println("start listening on port: " + port);

            while (isListening.get()) {
                SocketChannel socketChannel = serverSocketChannel.accept();
                handleLoginRequest(socketChannel);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readMessages() {
        for (int i = 0; i < fixSessions.size(); i++) {
            fixSessions.get(i)
                    .readMessage();
        }
    }


    private void handleLoginRequest(SocketChannel socketChannel) {
        try {
            System.out.println("handle login request");
            final int read = socketChannel.read(buf);
            buf.flip();
            try {
                switch (fixMessageParser.parseNextMessage(buf, incomingMessage)) {
                    case MSG_COMPLETE:
                        handleFixMessage(incomingMessage, socketChannel);
                        break;
                    default:
                        break;
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleFixMessage(FixMessage fixMessage, SocketChannel socketChannel) {
        switch (FixMessageType.lookup(fixMessage.messageType())) {
            case Logon:
                handleLogonMessage(fixMessage, socketChannel);
                break;
            default:
                System.out.println("unknown message type '" + fixMessage.messageType() + "' message ignored");
                break;

        }
    }

    private void handleLogonMessage(FixMessage fixMessage, SocketChannel socketChannel) {
        SimpleFixSession session = findSession(fixMessage.senderCompanyID(), fixMessage.senderSubID(), fixMessage.targetCompanyId(), fixMessage.targetSubId());

        final String sessionID = SimpleSessionConfig.sessionId(fixMessage.senderCompanyID(), fixMessage.senderSubID(), fixMessage.targetCompanyId(), fixMessage.targetSubId());

        System.out.println("logon request:\n" + FixMessage.asString(fixMessage));
        if (session != null) {
            try {
                session.linkToSocketChannel(socketChannel);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            session.sendSessionMessage(fixMessage);
            session.markAsConnected();
            System.out.println("Session connected '" + sessionID + "'");
        } else {
            System.out.println("Attempt to logon to unknown session '" + sessionID + "' ignored");
        }


    }

    private SimpleFixSession findSession(CharSequence senderCompanyId, CharSequence senderSubId, CharSequence targetCompanyId, CharSequence targetSubId) {
        for (int i = 0; i < fixSessions.size(); i++) {
            SimpleFixSession serverFixSession = fixSessions.get(i);
            if (serverFixSession.matches(senderCompanyId, senderSubId, targetCompanyId, targetSubId)) {
                return serverFixSession;
            }
        }
        return null;
    }

    public void broadcastToAllActiveClients(FixMessage fixMessage) {
        for (int i = 0; i < fixSessions.size(); i++) {
            SimpleFixSession currFixSession = fixSessions.get(i);
            try {
                currFixSession.sendMessage(fixMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
