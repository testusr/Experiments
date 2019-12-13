package smeo.experiments.monitoring.platform.jvm;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static smeo.experiments.monitoring.platform.jvm.ValueRecord.ValueType.DOUBLE;
import static smeo.experiments.monitoring.platform.jvm.ValueRecord.ValueType.NUMBER;

public class JmxVmMonitor {
    final String vmIdentifier;
    final String jmxConnectionString;
    private final JmxValueRecorder jmxValueRecorder;

    private final ValueRecord vmIdentifierEntry;
    public static final String VM_ID = "vm.id";

    Map<String, ValueRecord> jmxPollData = new HashMap<>();

    GcDataViaJmxNotifications gcDataViaJmxNotifications = new GcDataViaJmxNotifications();
    ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1);
    private MBeanServerConnection mbServerConnection;


    public JmxVmMonitor(String vmIdentifier, String jmxConnectionString)  {
        this.vmIdentifier = vmIdentifier;
        this.vmIdentifierEntry = new ValueRecord();
        this.vmIdentifierEntry.update(ValueRecord.ValueType.STRING, vmIdentifier);

        this.jmxConnectionString = jmxConnectionString;
        this.jmxValueRecorder = new JmxValueRecorder();

        jmxValueRecorder.recordObjValues("java.lang:type=Memory")
                .composite("HeapMemoryUsage")
                .attribute("committed", NUMBER).as("vm.memory.heap.committed")
                .attribute("used", NUMBER).as("vm.memory.heap.used");
        jmxValueRecorder.recordObjValues("java.lang:type=Threading")
                .attribute("ThreadCount", NUMBER).as("vm.threads");
        jmxValueRecorder.recordObjValues("java.lang:type=OperatingSystem")
                .attribute("ProcessCpuLoad", DOUBLE).as("vm.cpu");

        gcDataViaJmxNotifications.registerListener(this::updateData);
        updateValues();
    }


    private void updateValues() {
        try {
            jmxValueRecorder.recordValues(jmxPollData, getOrcreateServerConnection());
            updateData(jmxPollData);
        } catch (Exception e) {
            this.mbServerConnection = null;
            e.printStackTrace();
        }
    }

    private void updateData(Map<String, ValueRecord> data) {
        data.put(VM_ID, vmIdentifierEntry);
        dataUpdated(data);
    }

    public void dataUpdated(Map<String, ValueRecord> data) {
        System.out.println(data);
    }

    public void startMonitoring() {
        scheduledExecutorService.scheduleAtFixedRate(() -> updateValues(), 3,  3, TimeUnit.SECONDS);
        gcDataViaJmxNotifications.listenToGcEvents(mbServerConnection);
    }

    private MBeanServerConnection getOrcreateServerConnection() throws IOException {
        if (mbServerConnection == null) {
            JMXServiceURL url = new JMXServiceURL(String.format("service:jmx:rmi:///jndi/rmi://%s/jmxrmi", jmxConnectionString));
            JMXConnector jmxc = JMXConnectorFactory.connect(url, null);
            mbServerConnection = jmxc.getMBeanServerConnection();
        }
        return mbServerConnection;
    }


    public static void main(String[] args) {
        new JmxVmMonitor("testVm", "localhost:9010").startMonitoring();
        while (true){
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
