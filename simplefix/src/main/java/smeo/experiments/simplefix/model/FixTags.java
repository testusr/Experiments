package smeo.experiments.simplefix.model;

/*
 * We should generate the contents of this class
 */
public enum FixTags {
    Account("Account", 1),
    BeginString("BeginString", 8),
    BodyLength("BodyLength", 9),
    CheckSum("CheckSum", 10),
    ClOrdID("ClOrdID", 11),
    HandlInst("HandlInst", 21),
    MsgSeqNum("MsgSeqNum", 34),
    MsgType("MsgType", 35),
    SenderCompID("SenderCompID", 49),
    SenderSubID("SenderSubID", 50),
    SendingTime("SendingTime", 52),
    TargetCompID("TargetCompID", 56),
    TargetSubID("TargetSubID", 57),
    TimeInForce("TimeInForce", 59),
    OrderQty("OrderQty", 38),
    OrdType("OrdType", 40),
    PossDupFlag("PossDupFlag", 43),
    Price("Price", 44),
    Side("Side", 54),
    Symbol("Symbol", 55),
    Text("Text", 58),
    TransactTime("TransactTime", 60),
    EncryptMethod("EncryptMethod", 98),
    SecurityDesc("SecurityDesc", 107),
    HeartBtInt("HeartBtInt", 108),
    TestReqID("TestReqID", 112),
    QuoteReqID("QuoteReqID", 131),
    ResetSeqNumFlag("ResetSeqNumFlag", 141),
    SenderLocationID("SenderLocationID", 142),
    TargetLocationID("TargetLocationID", 143),
    SecurityType("SecurityType", 167),
    PartyIDSource("SecurityType", 447),
    PartyID("PartyID", 448),
    PartyRole("PartyRole", 452),
    // FIX 4.2
    FutSettDate("FutSettDate", 64),
    // TEST
    TestTag1("TestTag1", 9001),
    TestTag2("TestTag2", 9002),

    // Groups
    NoPartyIDs("NoPartyIDs", 453),
    NoRelatedSym("NoRelatedSym", 146),
    // TEST
    NoTestSubgroup("NoTestSubgroup", 9000), MDReqId("MDReqId", 262), NoMDEntries("NoMDEntries", 268), MDEntryType("MDEntryType", 269), MDEntryPx("MDEntryPx", 270), MDEntrySize("MDEntrySize", 271), MDEntryDate("MDEntryDate", 272), MDEntryTime("MDEntryTime", 273), MdQuoteType("MdQuoteType", 1070), MDEntryID("MDEntryID", 278);

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

    /**
     * Should be used in generated version
     */
    public enum Type {
        STRING,
        CHAR,
        TIMESTAMP,
        LONG,
        DOUBLE
    }

    public static void addTag(String string, int i) {
        // TODO Auto-generated method stub
        throw new RuntimeException("Not yet implemented");
    }
}
