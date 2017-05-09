package smeo.experiments.chronicle.replication.echo;

import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ChronicleQueueBuilder;
import net.openhft.chronicle.ExcerptAppender;
import net.openhft.chronicle.ExcerptTailer;

import java.io.IOException;

import static smeo.experiments.chronicle.replication.echo.EchoInitiator.ECHO_INITIATOR_PORT;
import static smeo.experiments.chronicle.replication.echo.EchoInitiator.ECHO_REFLECTOR_PORT;

public class EchoReflector {

	public static final String LOCALHOST = "localhost";

	public static void main(String[] args) {
		final EchoReflector echoReflection = new EchoReflector();
		try {
			echoReflection.relfectEchos(args);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void relfectEchos(String[] args) throws IOException {

		final String tmpDir = System.getProperty("java.io.tmpdir");
		String incomingDataChroniclePath = tmpDir + "/er_initiated_echos";
		String outgoingDataChroniclePath = tmpDir + "/er_reflected_echos";

		System.out.println("i:" + incomingDataChroniclePath);
		System.out.println("o:" + outgoingDataChroniclePath);

		String initiatorAdress = LOCALHOST;

		if (args.length == 1) {
			initiatorAdress = args[0];
		}

		final int initatorPort = ECHO_INITIATOR_PORT;

		System.out.println("waiting for echos to reflect on port '" + ECHO_REFLECTOR_PORT + "' back to " + initiatorAdress + ":" + initatorPort + " ");

		Chronicle outgoingDataChronicle = ChronicleQueueBuilder.indexed(incomingDataChroniclePath)
				.sink()
				.connectAddress(initiatorAdress, initatorPort)
				.build();
		final ExcerptTailer tailer = outgoingDataChronicle.createTailer();

		Chronicle incomingDataChronicle = ChronicleQueueBuilder.indexed(outgoingDataChroniclePath)
				.source()
				.bindAddress("localhost", ECHO_REFLECTOR_PORT).build();
		final ExcerptAppender appender = incomingDataChronicle.createAppender();

		EchoData echoData = new EchoData();

		boolean firstEcho = true;
		int i = 0;
		while (true) {
			if (tailer.nextIndex()) {
				try {
					echoData.readExternal(tailer);
					echoData.reflected();
					appender.startExcerpt();
					echoData.writeExternal(appender);
					appender.finish();
					if (firstEcho) {
						System.out.println("reflected first echo");
						firstEcho = false;
					}
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}

		}

	}
}
