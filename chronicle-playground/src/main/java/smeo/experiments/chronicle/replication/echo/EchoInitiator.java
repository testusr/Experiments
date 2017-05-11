package smeo.experiments.chronicle.replication.echo;

import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ChronicleQueueBuilder;
import net.openhft.chronicle.ExcerptAppender;
import smeo.experiments.chronicle.replication.echo.payload.PayloadBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Connecting to {@link EchoReceiver} to listen for reflected eche remotely and initiate
 * echos with payload at a preconfigured frequency.
 */
public class EchoInitiator {
	public static final String LOCALHOST = "localhost";
	final int PAYLOAD_NO_OF_BANDS = 60;

	public static int ECHO_INITIATOR_PORT = 12445;
	public static int ECHO_REFLECTOR_PORT = 12446;

	long echosPerSecond = 10000;

	AtomicBoolean isWritingEchos = new AtomicBoolean(true);

	private boolean readerStarted = false;
	int port_echo_reflector = ECHO_REFLECTOR_PORT;
	String address_echo_reflector = LOCALHOST;
	int local_port = ECHO_INITIATOR_PORT;
	String local_address = LOCALHOST;

	String chronicle_out_path = initatedEchosChroniclePath();
	String chronicle_in_path = reflectedEchosChroniclePath();
	String chronicle_result_path = receivedEchosChroniclePath();

	long lastSendEchoId = -1;

	public static String initatedEchosChroniclePath() {
		return System.getProperty("java.io.tmpdir") + "/e_initiated_echos";
	}

	public static String reflectedEchosChroniclePath() {
		return System.getProperty("java.io.tmpdir") + "/e_reflected_echos";
	}

	public static String receivedEchosChroniclePath() {
		return System.getProperty("java.io.tmpdir") + "/e_received_echos";
	}

	public void sendEchos(String[] args) throws IOException {
		processCommandLineArguments(args);
		printSettings(chronicle_result_path);

		EchoData echoObjectToSend = preallocateEchoDataObj();
		long nanoWaitTime = sendUpdateEveryXnanos(echosPerSecond);

		ExcerptAppender outgoingDataAppender = exposeInitiatedEchoChronicleToRemoteListeners(chronicle_out_path, local_port, local_address);
		startEchoReceiver(chronicle_in_path, chronicle_result_path);
		startLatencyReporter();
		waitSomeTime();

		System.out.println("start writing ");
		try {
			long nanosSendTime = -1;
			long nextEchoSendTime = -1;

			while (true) {
				if (System.nanoTime() >= nextEchoSendTime) {
					nanosSendTime = System.nanoTime();
					nextEchoSendTime = nanosSendTime + nanoWaitTime;
					outgoingDataAppender.startExcerpt();
					echoObjectToSend.newEchoCall();
					echoObjectToSend.writeExternal(outgoingDataAppender);
					outgoingDataAppender.finish();
				}
			}

		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private void printSettings(String chronicle_result_path) {
		System.out.println(chronicle_out_path);
		System.out.println(chronicle_in_path);

		System.out.println("EchoInitiator [<address_echo_reflector>:<port_echo_reflector>] <local_port>");
		System.out.println("- to address_echo_reflector: " + address_echo_reflector);
		System.out.println("- to port_echo_reflector: " + port_echo_reflector);
		System.out.println("- local_port: " + local_port);
		System.out.println("reveived echos are writtent to local chronicle: '" + chronicle_result_path + "'");
	}

	private void processCommandLineArguments(String[] args) {
		if (args.length > 0) {
			String[] elements = args[0].split(":");
			this.address_echo_reflector = elements[0];
			this.port_echo_reflector = Integer.parseInt(elements[1]);
		}

		if (args.length == 2) {
			String[] elements = args[1].split(":");
			this.local_address = elements[0];
			this.local_port = Integer.parseInt(elements[1]);
		}
	}

	private void waitSomeTime() {
		try {
			System.out.println("waiting 10s to start writer");
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private ExcerptAppender exposeInitiatedEchoChronicleToRemoteListeners(String chronicle_out_path, int local_port, String local_address) throws IOException {
		// CHRONICLE TO CONNECT TO REMOTE CLIENT
		Chronicle outgoingChronicle = ChronicleQueueBuilder.indexed(chronicle_out_path)
				.source()
				.bindAddress(local_address, local_port)
				.build();
		System.out.println("exposing echo calls to be read from " + local_address + ":" + local_port);
		return outgoingChronicle.createAppender();
	}

	public static EchoData preallocateEchoDataObj() {
		EchoData echoData = new EchoData();
		echoData.payload = PayloadBuilder.bigPriceUpdate(60);
		return echoData;
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

	private void startEchoReceiver(String incomingDataChroniclePath, String chronicle_result_path) throws IOException {
		if (!readerStarted) {
			Chronicle incomingDataChronicle = ChronicleQueueBuilder.indexed(incomingDataChroniclePath)
					.sink()
					.connectAddress(address_echo_reflector, port_echo_reflector)
					.build();
			Chronicle resultData = ChronicleQueueBuilder.indexed(chronicle_result_path)
					.build();
			System.out.println("connecting local sink to " + address_echo_reflector + ":" + port_echo_reflector + " to read reflected echos");
			new EchoReceiver(incomingDataChronicle, resultData.createAppender()).start();
			readerStarted = true;
		}
	}

	private void startLatencyReporter() {
		new EchoLatencyReporter().start();
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
