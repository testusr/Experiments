package smeo.experiments.chronicle.replication.echo;

import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ChronicleQueueBuilder;
import net.openhft.chronicle.ExcerptTailer;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Reading the received echos and report timings in a report.
 */
public class EchoLatencyReporter {
	public static final int SAMPLE_SIZE = 10000;

	EchoData[] echos_received = new EchoData[SAMPLE_SIZE];
	long[] meanLatencyMs = new long[SAMPLE_SIZE];
	long[] meanLatencyNanos = new long[SAMPLE_SIZE];

	public void start() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				System.out.println("latency reporter started");
				readResultsAndCreateReports();
			}
		}).start();
	}

	private void readResultsAndCreateReports() {
		try {
			prealloacteSampleEchos();
			Chronicle resultData = ChronicleQueueBuilder.indexed(EchoInitiator.receivedEchosChroniclePath())
					.build();
			final ExcerptTailer tailer = resultData.createTailer();
			int i = 0;
			while (true) {

				if (tailer.nextIndex()) {
					try {
						echos_received[i++].readExternal(tailer);
						if (i == SAMPLE_SIZE) {
							printReport(echos_received);
							i = 0;
						}
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
				} else {
					waitSomeTime();
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void waitSomeTime() {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void prealloacteSampleEchos() {
		for (int i = 0; i < echos_received.length; i++) {
			echos_received[i] = EchoInitiator.preallocateEchoDataObj();
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

		long timeNeededToSend = echos_received[echos_received.length - 1].tsEchoCalledNanos - echos_received[0].tsEchoCalledNanos;
		double sendFrequency = TimeUnit.SECONDS.toNanos(1) / (timeNeededToSend / (double) echos_received.length);

		for (int i = 0; i < echos_received.length; i++) {
			EchoData currEcho = echos_received[i];
			meanLatencyMs[i] = currEcho.meanLatencyMs();
			meanLatencyNanos[i] = currEcho.meanLatencyNanos();
		}
		Arrays.sort(meanLatencyMs);
		Arrays.sort(meanLatencyNanos);

		StringBuilder reportBuilder = new StringBuilder();
		reportBuilder.append("sendFrequency (per.Sec): ").append(sendFrequency).append("\n");
		reportBuilder.append("missingEchos: ").append(missingEchos).append("\n");
		reportBuilder.append("doubledEchos: ").append(doubledEchos).append("\n");
		reportBuilder.append("99th perc. mean ms: ").append(meanLatencyMs[(meanLatencyMs.length / 100) * 99]).append("\n");
		reportBuilder.append("99.9th perc. mean ms: ").append(meanLatencyMs[(int) Math.round((meanLatencyMs.length / 100) * 99.9)]).append("\n");
		reportBuilder.append("99th perc. mean nanos: ").append(meanLatencyNanos[(meanLatencyNanos.length / 100) * 99]).append("\n");
		reportBuilder.append("99.9th perc. mean nanos: ").append(meanLatencyNanos[(int) Math.round((meanLatencyMs.length / 100) * 99.9)]).append("\n");

		System.out.println(reportBuilder.toString());
	}
}
