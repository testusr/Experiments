package smeo.experiments.chronicle.replication.echo;

import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ChronicleQueueBuilder;
import net.openhft.chronicle.ExcerptAppender;
import net.openhft.chronicle.ExcerptTailer;

import java.io.IOException;

import static smeo.experiments.chronicle.replication.echo.EchoInitiator.ECHO_INITIATOR_PORT;
import static smeo.experiments.chronicle.replication.echo.EchoInitiator.ECHO_REFLECTOR_PORT;

/**
 * Listen remotely on {@link EchoInitiator} for initiated echos and send them right back (echo)
 * with receive time stamps.
 */
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
		String incomingDataChroniclePath = tmpDir + "/e_r_initiated_echos";
		String outgoingDataChroniclePath = tmpDir + "/e_r_reflected_echos";

		System.out.println("i:" + incomingDataChroniclePath);
		System.out.println("o:" + outgoingDataChroniclePath);

		String address_echo_initiator = LOCALHOST;
		int port_echo_initiator = ECHO_INITIATOR_PORT;

		int local_port = ECHO_REFLECTOR_PORT;
		String local_address = LOCALHOST;

		if (args.length > 0) {
			String[] elements = args[0].split(":");
			address_echo_initiator = elements[0];
			port_echo_initiator = Integer.parseInt(elements[1]);
		}

		if (args.length == 2) {
			String[] elements = args[1].split(":");
			local_address = elements[0];
			local_port = Integer.parseInt(elements[1]);
		}

		System.out.println("EchoReflector [<address_echo_initiator>:<port_echo_initiator>] <local_port>");
		System.out.println("- to address_echo_initiator: " + address_echo_initiator);
		System.out.println("- to port_echo_initiator: " + port_echo_initiator);
		System.out.println("- local_port: " + local_port);

		Chronicle incomingDataChronicle = ChronicleQueueBuilder.indexed(incomingDataChroniclePath)
				.sink()
				.connectAddress(address_echo_initiator, port_echo_initiator)
				.build();
		System.out.println("connecting sink to read echos from " + address_echo_initiator + ":" + port_echo_initiator);
		final ExcerptTailer tailer = incomingDataChronicle.createTailer();

		Chronicle outgoingDataChronicle = ChronicleQueueBuilder.indexed(outgoingDataChroniclePath)
				.source()
				.bindAddress(local_address, local_port).build();
		System.out.println("echo reflections exposed on " + local_address + ":" + local_port);
		final ExcerptAppender appender = outgoingDataChronicle.createAppender();

		EchoData echoData = EchoInitiator.preallocateEchoDataObj();

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
					if (i++ == 50000) {
						System.out.println("reflected " + i + " echos, last with id " + echoData.id);
						i = 0;
					}
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
