package smeo.experiments.monitoring.platform.jvm;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class JmxVmMonitorInfluxDb extends JmxVmMonitor {
    public static final String INFLUX_DB_NAME = "platform-stats";
    public static final String DEFAULT_POLICY = "defaultPolicy";
    private final String databaseUrl;
    private final String userName;
    private final String password;

    InfluxDB influxDB;

    public JmxVmMonitorInfluxDb(String vmIdentifier, String jmxConnectionString, String databaseUrl, String userName, String password) {
        super(vmIdentifier, jmxConnectionString);
        this.databaseUrl = databaseUrl;
        this.userName = userName;
        this.password = password;
        connectToInflux();
    }

    private void connectToInflux() {
        influxDB = InfluxDBFactory.connect(databaseUrl, userName, password);
        influxDB.createDatabase(INFLUX_DB_NAME);
        influxDB.createRetentionPolicy(DEFAULT_POLICY, INFLUX_DB_NAME, "30d", 1, true);
        influxDB.setDatabase(INFLUX_DB_NAME);
        influxDB.setRetentionPolicy(DEFAULT_POLICY);
    }

    @Override
    public void dataUpdated(Map<String, ValueRecord> data) {
        if (influxDB != null) {
            Point point = createPoint(data);
            influxDB.write(point);
        }
    }

    private Point createPoint(Map<String, ValueRecord> data) {
        Point.Builder builder = null;
        boolean isFirstField = true;
        String measurementTag = "";
        for (Map.Entry<String, ValueRecord> entry : data.entrySet()) {
            String key = entry.getKey();
            if (!key.equals(JmxVmMonitor.VM_ID)) {
                if (isFirstField) {
                    String[] keyElements = key.split("\\.");
                    builder = Point.measurement(keyElements[0]);
                    builder.time(entry.getValue().timestamp, TimeUnit.MILLISECONDS);
                    measurementTag = keyElements[0]+".";
                    isFirstField = false;
                }
                ValueRecord value = entry.getValue();

                switch (value.valueType) {
                    case DOUBLE:
                        builder.addField(removeMTag(measurementTag, key), value.doubleValue);
                        break;
                    case STRING:
                        builder.addField(removeMTag(measurementTag, key), value.stringValue);
                        break;
                    case NUMBER:
                        builder.addField(removeMTag(measurementTag, key), value.numberValue);
                        break;
                    case BOOLEAN:
                        builder.addField(removeMTag(measurementTag, key), value.booleanValue);
                        break;
                }
            }
        }
        ValueRecord valueRecord = data.get(JmxVmMonitor.VM_ID);
        if (valueRecord != null) {
            builder.tag("vm", valueRecord.stringValue);
        }
        return builder.build();
    }

    private String removeMTag(String measurementTag, String key) {
        return key.replace(measurementTag, "").replace(".", "_");
    }

    public static void main(String[] args) {
        new JmxVmMonitorInfluxDb("testVm",
                "localhost:9010",
                "http://localhost:8086",
                "admin","admin").startMonitoring();
        while (true){
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
