package smeo.experiments.chronicle.replication.echo;

import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ChronicleQueueBuilder;
import net.openhft.chronicle.ExcerptAppender;
import net.openhft.chronicle.ExcerptTailer;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class EchoInitiator {
	public static final String LOCALHOST = "localhost";
	public static int ECHO_INITIATOR_PORT = 12445;
	public static int ECHO_REFLECTOR_PORT = 12446;

	public static final int NO_OF_ECHOS = 10000;
	EchoData[] echos_received = new EchoData[NO_OF_ECHOS];
	long[] meanLatencyMs = new long[NO_OF_ECHOS];
	long[] meanLatencyNanos = new long[NO_OF_ECHOS];

	long echosPerSecond = 40000;

	AtomicBoolean isWritingEchos = new AtomicBoolean(true);

	private boolean readerStarted = false;
	final int sinkConnectedTo = ECHO_REFLECTOR_PORT;
	long lastSendEchoId = -1;

	public void sendEchos(String[] args) throws IOException {

		final String tmpDir = System.getProperty("java.io.tmpdir");
		String chronicle_out_path = tmpDir + "/e_initiated_echos";
		String chronicle_in_path = tmpDir + "/e_reflected_echos";

		System.out.println(chronicle_out_path);
		System.out.println(chronicle_in_path);

		int outgoingSourcePort = ECHO_INITIATOR_PORT;
		String outgoingSourceAdress = LOCALHOST;

		if (args.length == 1) {
			outgoingSourceAdress = args[0];
		}

		System.out.println("sending echo to be reflected from " + outgoingSourceAdress + ":" + outgoingSourcePort
				+ " and waiting for reflections on localhost:"
				+ sinkConnectedTo + "");

		// CHRONICLE TO CONNECT TO REMOTE CLIENT
		Chronicle outgoingChronicle = ChronicleQueueBuilder.indexed(chronicle_out_path)
				.source()
				.bindAddress(outgoingSourceAdress, outgoingSourcePort)
				.build();
		System.out.println("exposing echo calls to be read from " + outgoingSourceAdress + ":" + outgoingSourcePort);
		ExcerptAppender outgoingDataAppender = outgoingChronicle.createAppender();

		for (int i = 0; i < echos_received.length; i++) {
			echos_received[i] = new EchoData();
		}

		startReader(chronicle_in_path);
		try {
			System.out.println("waiting 10s to start writer");
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		EchoData echo = new EchoData();

		long nanoWaitTime = sendUpdateEveryXnanos(echosPerSecond);
		while (true) {
			System.out.println("start writing ");
			int i = 0;
			long nanosSendTime = -1;
			long nextEchoSendTime = -1;
			while (isWritingEchos.get()) {
				if (System.nanoTime() >= nextEchoSendTime) {
					nanosSendTime = System.nanoTime();
					nextEchoSendTime = nanosSendTime + nanoWaitTime;
					outgoingDataAppender.startExcerpt();
					echo.newEchoCall();
					echo.writeExternal(outgoingDataAppender);
					outgoingDataAppender.finish();
					i++;
				}
			}
			System.out.println("writing stopped after " + i + " echos");
			waitTillWritingEnabledAgain();

		}

	}

	private long sendUpdateEveryXnanos(long echosPerSecond) {
		return (TimeUnit.SECONDS.toNanos(1) / echosPerSecond);
	}

	private void waitTillWritingEnabledAgain() {
		while (!isWritingEchos.get()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void startReader(String incomingDataChroniclePath) throws IOException {
		if (!readerStarted) {
			Chronicle incomingDataChronicle = ChronicleQueueBuilder.indexed(incomingDataChroniclePath)
					.sink()
					.connectAddress(LOCALHOST, sinkConnectedTo)
					.build();
			System.out.println("connecting local sink to " + LOCALHOST + ":" + ECHO_REFLECTOR_PORT + " to read reflected echos");
			new ChronicleEchoReader(incomingDataChronicle).start();
			readerStarted = true;
		}
	}

	private class ChronicleEchoReader {
		final ExcerptTailer tailer;

		public ChronicleEchoReader(Chronicle incomingDataChronicle) throws IOException {
			tailer = incomingDataChronicle.createTailer().toEnd();
		}

		public void start() {
			new Thread(new Runnable() {
				EchoData dummyEcho = new EchoData();

				@Override
				public void run() {
					while (true) {
						System.out.println("respawning read");
						// giving it a few packages to establish the flow
						final int warmupEchos = 1000;
						int i = -warmupEchos;
						isWritingEchos.set(true);
						while (i < echos_received.length) {
							if (tailer.nextIndex()) {
								try {
									if (i >= 0) {
										echos_received[i].readExternal(tailer);
										echos_received[i].relectionReceived();
									}
									i++;
								} catch (IOException e) {
									e.printStackTrace();
								} catch (ClassNotFoundException e) {
									e.printStackTrace();
								}
							}
						}

						System.out.println("enough samples read (" + i + ") + '" + warmupEchos + "' warmup echos");
						isWritingEchos.set(false);
						printReport(echos_received);
						waitTillNoEchosAreReceivedAnymoreForSec(3);
						tailer.toEnd();
					}
				}

				private void waitTillNoEchosAreReceivedAnymoreForSec(int waitTimeInSec) {
					System.out.println("wait until no echos received for '" + waitTimeInSec + "' in sec");
					long lastEchoReceived = System.currentTimeMillis();
					long waitTimeInMs = TimeUnit.SECONDS.toMillis(waitTimeInSec);
					while ((System.currentTimeMillis() - lastEchoReceived) < waitTimeInMs) {
						if (tailer.nextIndex()) {
							lastEchoReceived = System.currentTimeMillis();
							try {
								dummyEcho.readExternal(tailer);
							} catch (IOException e) {
								e.printStackTrace();
							} catch (ClassNotFoundException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}).start();
		}
	}

	private void printReport(EchoData[] echos_received) {
		long missingEchos = 0;
		long doubledEchos = 0;
		for (int i = 1; i < echos_received.length; i++) {
			long idDiff = echos_received[i].id - echos_received[i - 1].id;
			if (idDiff == 0) {
				doubledEchos++;
			}
			if (idDiff > 1) {
				missingEchos += (idDiff - 1);
			}
		}
		for (int i = 0; i < echos_received.length; i++) {
			EchoData currEcho = echos_received[i];
			meanLatencyMs[i] = currEcho.meanLatencyMs();
			meanLatencyNanos[i] = currEcho.meanLatencyNanos();
		}
		Arrays.sort(meanLatencyMs);
		Arrays.sort(meanLatencyNanos);

		StringBuilder reportBuilder = new StringBuilder();
		reportBuilder.append("missingEchos: ").append(missingEchos).append("\n");
		reportBuilder.append("doubledEchos: ").append(doubledEchos).append("\n");
		reportBuilder.append("99th perc. mean ms: ").append(meanLatencyMs[(meanLatencyMs.length / 100) * 99]).append("\n");
		reportBuilder.append("99.9th perc. mean ms: ").append(meanLatencyMs[(int) Math.round((meanLatencyMs.length / 100) * 99.9)]).append("\n");
		reportBuilder.append("99th perc. mean nanos: ").append(meanLatencyNanos[(meanLatencyNanos.length / 100) * 99]).append("\n");
		reportBuilder.append("99.9th perc. mean nanos: ").append(meanLatencyNanos[(int) Math.round((meanLatencyMs.length / 100) * 99.9)]).append("\n");

		System.out.println(reportBuilder.toString());
	}

	public static void main(String[] args) {
		final EchoInitiator echoReflection = new EchoInitiator();
		try {
			echoReflection.sendEchos(args);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
