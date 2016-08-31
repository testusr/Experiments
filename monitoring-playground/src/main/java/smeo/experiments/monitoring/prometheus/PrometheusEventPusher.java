package smeo.experiments.monitoring.prometheus;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import io.prometheus.client.exporter.PushGateway;

import java.io.IOException;
import java.util.Random;

/**
 * Attempt to write a prometheu push exporter
 * <p>
 * docker run -p 9090:9090 -v /tmp/prometheus.yml:/etc/prometheus/prometheus.yml prom/prometheus
 * dockerun -d -p 9100:9100 --net="host" prom/node-exporter
 */
public class PrometheusEventPusher {
    static final Random random = new Random(System.currentTimeMillis());
    Gauge percentileGauge;
    PushGateway pushGateway = new PushGateway("127.0.0.1:9091");

    public void start() {
        percentileGauge = Gauge.build().name("percentiles").labelNames("percentile", "unit").help("component message end to end latency")
                .register();
        while (true) {
            reportMetaData();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void reportMetaData() {
        int[] randomValues = randomValues(10, 10, 10, 20);
        printPrecentile(99.9, randomValues[3]);
        printPrecentile(99, randomValues[2]);
        printPrecentile(50, randomValues[1]);
        printPrecentile(30, randomValues[0]);

        try {
            pushGateway.push(CollectorRegistry.defaultRegistry, "test-latencies");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printPrecentile(double perecentile, int value) {
        final Gauge.Child child = new Gauge.Child();
        final Collector collector = percentileGauge.setChild(child, "p" + perecentile + "th", "millis");
        child.set(value/10.0);
    }


    static int[] randomValues(int... randomBounds) {
        int[] results = new int[randomBounds.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = random.nextInt(randomBounds[i]) + (i > 0 ? results[i - 1] : 0);
        }
        return results;
    }

    public static void main(String[] args) {
        PrometheusEventPusher latencyPrinter = new PrometheusEventPusher();
        latencyPrinter.start();
    }
}
