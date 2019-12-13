package smeo.experiments.monitoring.platform.jvm;

import com.sun.management.GarbageCollectionNotificationInfo;

import javax.management.*;
import javax.management.openmbean.CompositeData;
import java.io.IOException;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.*;

import static smeo.experiments.monitoring.platform.jvm.ValueRecord.ValueType.*;

public class GcDataViaJmxNotifications {
    Map<String, ValueRecord> data = new HashMap<>();
    volatile GcListener gcListener;

    // http://www.fasterj.com/articles/gcnotifs.shtml
    public void listenToGcEvents(MBeanServerConnection connection) {
        try {
            List<GarbageCollectorMXBean> beans = getGarbageCollectorMXBeansFromRemote(connection);
            for (GarbageCollectorMXBean bean : beans) {
                NotificationEmitter emitter = (NotificationEmitter) bean;
                emitter.addNotificationListener(this::recordData, null, null);
            }
        } catch (Exception e) {
        }
    }

    public void registerListener(GcListener listener){
        this.gcListener = listener;
    }

    private static List<GarbageCollectorMXBean> getGarbageCollectorMXBeansFromRemote(MBeanServerConnection mBeanServerConn)
            throws MalformedObjectNameException, NullPointerException, IOException {

        List<GarbageCollectorMXBean> gcMXBeans = new ArrayList<>();
        ObjectName gcAllObjectName = new ObjectName(ManagementFactory.GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE + ",*");
        Set<ObjectName> gcMXBeanObjectNames = mBeanServerConn.queryNames(gcAllObjectName, null);
        for (ObjectName on : gcMXBeanObjectNames) {
            GarbageCollectorMXBean gc = ManagementFactory.newPlatformMXBeanProxy(
                    mBeanServerConn, on.getCanonicalName(), GarbageCollectorMXBean.class);
            gcMXBeans.add(gc);
        }
        return gcMXBeans;
    }

    private void recordData(Notification notification, Object obj) {
        if (notification.getType().equals(GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION)) {

            GarbageCollectionNotificationInfo info = GarbageCollectionNotificationInfo
                    .from((CompositeData) notification.getUserData());

            long duration = info.getGcInfo().getDuration();
            long timestamp = notification.getTimeStamp() - duration;

            recordDataEntry("gc.type", info.getGcName(), timestamp);
            recordDataEntry("gc.cause", info.getGcCause(), timestamp);
            recordDataEntry("gc.action", info.getGcAction(), timestamp);
            recordDataEntry("gc.duration", info.getGcInfo().getDuration(), timestamp);

            GcListener listener = gcListener;
            if (listener != null){
                listener.gvEvent(data);
            }
        }
    }

    private void recordDataEntry(String key, long value, long timestamp) {
        ValueRecord valueRecord = data.computeIfAbsent(key, e -> new ValueRecord());
        valueRecord.update(NUMBER, value);
        valueRecord.setTimestamp(timestamp);
    }

    private void recordDataEntry(String key, String value, long timestamp) {
        ValueRecord valueRecord = data.computeIfAbsent(key, e -> new ValueRecord());
        valueRecord.update(STRING, value);
        valueRecord.setTimestamp(timestamp);
    }

    public interface GcListener {
        void gvEvent(Map<String, ValueRecord> data);
    }
}