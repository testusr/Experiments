package smeo.experiments.monitorable;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Simple app with no dependencies doing something,
 * creating garbage using up some cpu
 */
public class Monitorable implements MonitorableMBean {
    private volatile boolean isRunning = true;
    List<String> randomStore = new ArrayList<>();

    public void run() {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        ObjectName objectName = null;
        try {
            objectName = new ObjectName("smeo.experiments.monitorable", "name", "sillybean");
            server.registerMBean(this, objectName);
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        } catch (NotCompliantMBeanException e) {
            e.printStackTrace();
        } catch (InstanceAlreadyExistsException e) {
            e.printStackTrace();
        } catch (MBeanRegistrationException e) {
            e.printStackTrace();
        }

        while (isRunning){
            Random random = new Random();

            if (random.nextBoolean() || randomStore.size() > 10000){
                randomStore.clear();
            }
            long endTime = System.currentTimeMillis() + Math.abs(random.nextInt() % 4000);
            while (System.currentTimeMillis() < endTime) {
                Math.round(random.nextDouble() / random.nextFloat());
                randomStore.add(String.valueOf(UUID.randomUUID()));
            }
            try {
                Thread.sleep(Math.abs(random.nextLong()% 100));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }



    @Override
    public void setStop(boolean val) {
        isRunning = val;
    }

    @Override
    public boolean getStop() {
        return isRunning;
    }

    @Override
    public int getIntValue() {
        return randomStore.size();
    }

    @Override
    public void setIntValue(int val) {

    }

    @Override
    public String getStringValue() {
        try {
            return randomStore.get(0);
        } catch (Exception e){
            return e.getMessage();
        }
    }

    @Override
    public void setStringValue(String val) {

    }

    public static void main(String[] args) {
        new Monitorable().run();;
    }
}
