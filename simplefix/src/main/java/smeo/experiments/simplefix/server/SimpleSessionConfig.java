package smeo.experiments.simplefix.server;

public class SimpleSessionConfig {
    final String beginString;
    final String senderCompID;
    final String senderSubID;
    final String targetCompID;
    final String targetSubID;

    public SimpleSessionConfig(String beginString, String senderCompID, String senderSubID, String targetCompID, String targetSubID) {
        this.beginString = beginString;
        this.senderCompID = senderCompID;
        this.senderSubID = senderSubID;
        this.targetCompID = targetCompID;
        this.targetSubID = targetSubID;
    }

    public static SimpleSessionConfigBuilder builder() {
        return new SimpleSessionConfigBuilder();
    }

    public String beginString() {
        return beginString;
    }

    public String senderCompID() {
        return senderCompID;
    }

    public String senderSubID() {
        return senderSubID;
    }

    public String targetCompID() {
        return targetCompID;
    }

    public String targetSubID() {
        return targetSubID;
    }

    public static String sessionId(CharSequence senderCompanyId, CharSequence senderSubId, CharSequence targetCompanyId, CharSequence targetSubId) {
        return senderCompanyId + ":" + senderSubId + "->" + targetCompanyId + ":" + targetSubId;
    }

    @Override
    public String toString() {
        return sessionId(senderCompID, senderSubID, targetCompID, targetSubID);
    }

    public static class SimpleSessionConfigBuilder {
        private String beginString;
        private String senderCompID;
        private String senderSubID;
        private String targetCompID;
        private String targetSubID;

        public SimpleSessionConfigBuilder beginString(String beginString) {
            this.beginString = beginString;
            return this;
        }

        public SimpleSessionConfigBuilder senderCompID(String senderCompID) {
            this.senderCompID = senderCompID;
            return this;
        }

        public SimpleSessionConfigBuilder senderSubID(String senderSubID) {
            this.senderSubID = senderSubID;
            return this;
        }

        public SimpleSessionConfigBuilder targetCompID(String targetCompID) {
            this.targetCompID = targetCompID;
            return this;
        }

        public SimpleSessionConfigBuilder targetSubID(String targetSubID) {
            this.targetSubID = targetSubID;
            return this;
        }


        public SimpleSessionConfig build() {
            return new SimpleSessionConfig(beginString, senderCompID, senderSubID, targetCompID, targetSubID);
        }
    }
}
