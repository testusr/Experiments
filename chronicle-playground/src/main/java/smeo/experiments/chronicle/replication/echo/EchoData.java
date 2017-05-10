package smeo.experiments.chronicle.replication.echo;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by truehl on 09.05.17.
 */
public class EchoData implements Externalizable {
	private static AtomicLong echoCounter = new AtomicLong(0);

	public long id;
	public long clientSendMs;
	public long clientSendNanos;
	public long serverReceivedMs;
	public long serverReceivedNanos;
	public long clientReceivedMs;
	public long clientReceivedNanos;

	public EchoData() {
		refurbish();
	}

	public void refurbish() {
		clear();
		id = echoCounter.getAndIncrement();
	}

	public void clear() {
		clientSendMs = -1;
		clientSendNanos = -1;
		serverReceivedMs = -1;
		serverReceivedNanos = -1;
		clientReceivedMs = -1;
		clientReceivedNanos = -1;
	}

	public void echoCalled() {
		clear();
		clientSendMs = System.currentTimeMillis();
		clientSendNanos = System.nanoTime();
	}

	public void reflected() {
		serverReceivedMs = System.currentTimeMillis();
		serverReceivedNanos = System.nanoTime();
	}

	public void relectionReceived() {
		clientReceivedMs = System.currentTimeMillis();
		clientReceivedNanos = System.nanoTime();
	}

	@Override
	public void writeExternal(ObjectOutput bytes) throws IOException {
		bytes.writeLong(id);
		bytes.writeLong(clientSendMs);
		bytes.writeLong(clientSendNanos);
		bytes.writeLong(serverReceivedMs);
		bytes.writeLong(serverReceivedNanos);
		bytes.writeLong(clientReceivedMs);
		bytes.writeLong(clientReceivedNanos);
	}

	@Override
	public void readExternal(ObjectInput bytes) throws IOException, ClassNotFoundException {
		id = bytes.readLong();
		clientSendMs = bytes.readLong();
		clientSendNanos = bytes.readLong();
		serverReceivedMs = bytes.readLong();
		serverReceivedNanos = bytes.readLong();
		clientReceivedMs = bytes.readLong();
		clientReceivedNanos = bytes.readLong();
	}
}
