package smeo.experiments.monitoring.influxdb;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Use grafana and influxdb to test
 * docker run -d -p 8083:8083 -p 8086:8086 --expose 8090 --expose 8099 --name influxdb tutum/influxdb:0.10
 * 
 * Playing with the structure of metadata with tags and fields to have good performing
 * and expressive grafana visualization.
 * 
 */
public class InfluxDbMockDataWriter {
	static InfluxDB influxDB;
	static final String dbName = "WriterTest";
	static final Random random = new Random(System.currentTimeMillis());
	static long writtenPoints = 0;
	static final String[] liquidityLevels = { "1m", "2m", "5m", "10m", "15m", "20m", "40m" };

	static long lastWrittePointsTs = System.currentTimeMillis();

	public static void main(String[] args) {
		influxDB = InfluxDBFactory.connect("http://localhost:8086", "root", "root");
		influxDB.createDatabase(dbName);

		// Flush every 2000 Points, at least every 100ms
		influxDB.enableBatch(1000, 100, TimeUnit.MILLISECONDS);
		int loops = 0;
		while (true) {
			writeIncomingQuotes("bank1", "bank2", "bank3", "bank4", "bank5", "bank6", "bank7", "bank8", "bank9", "bank10",
					"bank11", "bank12", "bank13", "bank14", "bank15", "bank16", "bank17", "bank18", "bank19", "bank20",
					"bank21", "bank22", "bank23", "bank24", "bank25", "bank26", "bank27", "bank28", "bank29", "bank30",
					"bank31", "bank32", "bank33", "bank34", "bank35", "bank36", "bank37", "bank38", "bank39", "bank30");
			writePointsPerSecondIfNecessary();
		}

	}

	private static void writePointsPerSecondIfNecessary() {
		if (System.currentTimeMillis() - lastWrittePointsTs >= 1000) {
			final Point.Builder measurement = Point.measurement("pointsPerSecond")
					.addField("pointsWritten", writtenPoints);
			writeToInfluxDB(Point.measurement("perSecond").time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
					.tag("type", "influx_points").addField("count", writtenPoints).build());
			System.out.println("points per second: " + writtenPoints);
			writtenPoints = 0;
			lastWrittePointsTs = System.currentTimeMillis();
		}
	}

	public static void writeIncomingQuotes(String... banks) {
		int bands = liquidityLevels.length;
		int[] bandRandomBounds = new int[bands];
		for (int i = 0; i < bands; i++) {
			bandRandomBounds[i] = 10;
		}
		for (int iBank = 0; iBank < banks.length; iBank++) {
			writeMetaData();
			String currBank = banks[iBank];
			// rise the price level for each band to show clearer differences
			// when graphing
			for (int i = 1; i < bands; i++) {
				bandRandomBounds[i] += 5;
			}
			bandRandomBounds[0] += 10;

			int[] randoms = randomValues(bandRandomBounds);

			for (int i = 0; i < randoms.length; i++) {
				final Point.Builder measurement = Point.measurement("price")
						.tag("liquidity", String.valueOf(liquidityLevels[i]))
						.tag("bank", currBank)
						.addField("bid", (float) randoms[i])
						.addField("ask", (float) randoms[i] - random.nextInt(6));
				writeToInfluxDB(measurement.build());
			}

		}
	}

	public static void writeOutgoigQuotes(String... transformations) {

	}

	static int[] randomValues(int... randomBounds) {
		int[] results = new int[randomBounds.length];
		for (int i = 0; i < results.length; i++) {
			results[i] = random.nextInt(randomBounds[i]) + (i > 0 ? results[i - 1] : 0);
		}
		return results;
	}

	static public void writeMetaData() {
		int[] randomValues = randomValues(10, 10, 20, 30);
		final long currTime = currTime();
		writeToInfluxDB(Point.measurement("latency").time(currTime, TimeUnit.MILLISECONDS)
				.tag("percentile", "99th").addField("ns", randomValues[3]).build());
		writeToInfluxDB(Point.measurement("latency").time(currTime, TimeUnit.MILLISECONDS)
				.tag("percentile", "90th").addField("ns", randomValues[2]).build());
		writeToInfluxDB(Point.measurement("latency").time(currTime, TimeUnit.MILLISECONDS)
				.tag("percentile", "50th").addField("ns", randomValues[1]).build());
		writeToInfluxDB(Point.measurement("latency").time(currTime, TimeUnit.MILLISECONDS)
				.tag("percentile", "30th").addField("ns", randomValues[0]).build());

	}

	static private void writeToInfluxDB(Point point) {
		writtenPoints++;
		boolean written = false;
		int retries = 0;
		while (!written) {
			try {
				influxDB.write(dbName, "default", point);
				written = true;
			} catch (Exception e) {
			}
		}
	}

	static private long currTime() {
		return System.currentTimeMillis();
	}

}
