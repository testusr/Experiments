package smeo.experiments.monitorable;

public interface MonitorableMBean {
    void setStop(boolean val);
    boolean getStop();
    int getIntValue();
    void setIntValue(int val);
    String getStringValue();
    void setStringValue(String val);
}
