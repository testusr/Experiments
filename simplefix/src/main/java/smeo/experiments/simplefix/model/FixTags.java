package smeo.experiments.simplefix.model;

/*
 * We should generate the contents of this class
 */
public enum FixTags {
    BeginString("BeginString", 8),
    BodyLength("BodyLength", 9),
    CheckSum("CheckSum", 10),
    MsgType("MsgType", 35),
    SenderCompID("SenderCompID", 49),
    SenderSubID("SenderSubID", 50),
    TargetCompID("TargetCompID", 56),
    TargetSubID("TargetSubID", 57),
    Symbol("Symbol", 55);

    public final String name;

    public final int tag;

    FixTags(String name, int t) {
        this.name = name;
        this.tag = t;
    }

    public enum FixVersions {
        VERSION_44("FIX.4.4"),
        VERSION_50("FIXT.1.1");

        public final String beginString;

        FixVersions(String beginString) {
            this.beginString = beginString;
        }
    }

}
