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
	EchoData[] echos_send = new EchoData[NO_OF_ECHOS];
	EchoData[] echos_received = new EchoData[NO_OF_ECHOS];

	AtomicBoolean everythingRead = new AtomicBoolean(false);
	AtomicBoolean echoIsRunning = new AtomicBoolean(true);
	private boolean readerStarted = false;
	final int receivingPort = ECHO_INITIATOR_PORT;

	public void sendEchos(String[] args) throws IOException {

		final String tmpDir = System.getProperty("java.io.tmpdir");
		String chronicle_out_path = tmpDir + "/e_initiated_echos";
		String chronicle_in_path = tmpDir + "/e_reflected_echos";

		System.out.println(chronicle_out_path);
		System.out.println(chronicle_in_path);

		int reflectorPort = ECHO_REFLECTOR_PORT;
		String reflectorAdress = "localhost";

		if (args.length == 1) {
			reflectorAdress = args[0];
		}

		System.out.println("sending echo to be reflected from " + reflectorAdress + ":" + reflectorPort + " and waiting for reflections on localhost:"
				+ receivingPort + "");

		// CHRONICLE TO CONNECT TO REMOTE CLIENT
		Chronicle outgoingChronicle = ChronicleQueueBuilder.indexed(chronicle_out_path)
				.source()
				.connectAddress(reflectorAdress, reflectorPort)
				.build();
		ExcerptAppender outgoingDataAppender = outgoingChronicle.createAppender();

		for (int i = 0; i < echos_send.length; i++) {
			echos_send[i] = new EchoData();
		}
		for (int i = 0; i < echos_send.length; i++) {
			echos_received[i] = new EchoData();
		}

		startReader(chronicle_in_path);
		try {
			System.out.println("waiting 10s to start writer");
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		while (true) {
			everythingRead.set(false);
			System.out.println("start writing " + echos_send.length + " echos");

			for (int i = 0; i < echos_send.length; i++) {
				outgoingDataAppender.startExcerpt();
				echos_send[i].echoCalled();
				echos_send[i].writeExternal(outgoingDataAppender);
				outgoingDataAppender.finish();
			}

			for (int i = 0; i < echos_send.length; i++) {
				echos_send[i].refurbish();
			}

			System.out.println("waiting for echos beeing read");

			while (!everythingRead.get()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}

	}

	private void startReader(String incomingDataChroniclePath) throws IOException {
		if (!readerStarted) {
			Chronicle incomingDataChronicle = ChronicleQueueBuilder.indexed(incomingDataChroniclePath)
					.sink()
					.bindAddress(LOCALHOST, receivingPort)
					.build();

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
					System.out.println("starting reader");
					int i = 0;

					while (echoIsRunning.get()) {
						while (i < echos_send.length) {
							if (tailer.nextIndex()) {
								try {
									echos_received[i].readExternal(tailer);
									System.out.println("read echo '" + echos_received[i].id + "'");
									i++;
								} catch (IOException e) {
									e.printStackTrace();
								} catch (ClassNotFoundException e) {
									e.printStackTrace();
								}
							}
						}

						for (int j = 0; j < echos_received.length; j++) {
							echos_received[j].clear();
						}
						System.out.println("all echos read");
						everythingRead.set(true);

					}
				}
			}).start();
		}
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
