package smeo.experiments.chronicle.replication.echo;

import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ChronicleQueueBuilder;
import net.openhft.chronicle.ExcerptAppender;
import net.openhft.chronicle.ExcerptTailer;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class EchoInitiator {
	public static final String LOCALHOST = "localhost";
	public static int ECHO_INITIATOR_PORT = 12445;
	public static int ECHO_REFLECTOR_PORT = 12446;

	public static final int NO_OF_ECHOS = 10000;
	EchoData[] echos_send = new EchoData[NO_OF_ECHOS * 2];
	EchoData[] echos_received = new EchoData[NO_OF_ECHOS];

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
		System.out.println("local source bound to " + outgoingSourceAdress + ":" + outgoingSourcePort);
		ExcerptAppender outgoingDataAppender = outgoingChronicle.createAppender();

		for (int i = 0; i < echos_received.length; i++) {
			echos_received[i] = new EchoData();
		}

		for (int i = 0; i < echos_send.length; i++) {
			echos_send[i] = new EchoData();
		}

		startReader(chronicle_in_path);
		try {
			System.out.println("waiting 10s to start writer");
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		while (true) {
			System.out.println("start writing ");
			int i = 0;
			while (isWritingEchos.get()) {
				int sendIndex = i % echos_send.length;
				outgoingDataAppender.startExcerpt();
				echos_send[sendIndex].echoCalled();
				echos_send[sendIndex].writeExternal(outgoingDataAppender);
				outgoingDataAppender.finish();
				i++;
			}
			System.out.println("writing stopped after " + i + " echos");

			refurbishPreallocatedSendEchos();
			waitTillWritingEnabledAgain();

		}

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

	private void refurbishPreallocatedSendEchos() {
		for (int j = 0; j < echos_send.length; j++) {
			echos_send[j].refurbish();
		}
	}

	private void startReader(String incomingDataChroniclePath) throws IOException {
		if (!readerStarted) {
			Chronicle incomingDataChronicle = ChronicleQueueBuilder.indexed(incomingDataChroniclePath)
					.sink()
					.connectAddress(LOCALHOST, sinkConnectedTo)
					.build();
			System.out.println("local source bound to " + LOCALHOST + ":" + ECHO_REFLECTOR_PORT);
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
				@Override
				public void run() {
					while (true) {
						System.out.println("respawning read");
						// giving it a few packages to establish the flow
						int i = -1000;
						isWritingEchos.set(true);
						while (i < echos_received.length) {
							if (tailer.nextIndex()) {
								try {
									if (i >= 0) {
										echos_received[i].readExternal(tailer);
									}
									i++;
								} catch (IOException e) {
									e.printStackTrace();
								} catch (ClassNotFoundException e) {
									e.printStackTrace();
								}
							}
						}

						System.out.println("enough samples read (" + i + ") ");
						isWritingEchos.set(false);
						printReport(echos_received);
						tailer.toEnd();
					}
				}
			}).start();
		}
	}

	private void printReport(EchoData[] echos_received) {
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
