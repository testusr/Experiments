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
	public long tsEchoCalledMs;
	public long tsEchoCalledNanos;
	public long tsEchoReflectedMs;
	public long tsEchoReflectedNanos;
	public long tsReflectionReceivedMs;
	public long tsReflectionReceivedNanos;

	public EchoData() {
		clear();
	}

	public void clear() {
		id = -1;
		tsEchoCalledMs = -1;
		tsEchoCalledNanos = -1;
		tsEchoReflectedMs = -1;
		tsEchoReflectedNanos = -1;
		tsReflectionReceivedMs = -1;
		tsReflectionReceivedNanos = -1;
	}

	public void newEchoCall() {
		id = echoCounter.getAndIncrement();
		tsEchoCalledMs = System.currentTimeMillis();
		tsEchoCalledNanos = System.nanoTime();
		tsEchoReflectedMs = -1;
		tsEchoReflectedNanos = -1;
		tsReflectionReceivedMs = -1;
		tsReflectionReceivedNanos = -1;
	}

	public void reflected() {
		tsEchoReflectedMs = System.currentTimeMillis();
		tsEchoReflectedNanos = System.nanoTime();
	}

	public void relectionReceived() {
		tsReflectionReceivedMs = System.currentTimeMillis();
		tsReflectionReceivedNanos = System.nanoTime();
	}

	@Override
	public void writeExternal(ObjectOutput bytes) throws IOException {
		bytes.writeLong(id);
		bytes.writeLong(tsEchoCalledMs);
		bytes.writeLong(tsEchoCalledNanos);
		bytes.writeLong(tsEchoReflectedMs);
		bytes.writeLong(tsEchoReflectedNanos);
		bytes.writeLong(tsReflectionReceivedMs);
		bytes.writeLong(tsReflectionReceivedNanos);
	}

	@Override
	public void readExternal(ObjectInput bytes) throws IOException, ClassNotFoundException {
		id = bytes.readLong();
		tsEchoCalledMs = bytes.readLong();
		tsEchoCalledNanos = bytes.readLong();
		tsEchoReflectedMs = bytes.readLong();
		tsEchoReflectedNanos = bytes.readLong();
		tsReflectionReceivedMs = bytes.readLong();
		tsReflectionReceivedNanos = bytes.readLong();
	}

	public long meanLatencyMs() {
		return (tsReflectionReceivedMs - tsEchoCalledMs) / 2;
	}

	public long meanLatencyNanos() {
		return (tsReflectionReceivedNanos - tsEchoCalledNanos) / 2;
	}

}
