package smeo.experiments.simplefix.model;

import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;

public class SimpleFixMessageTest {

    public static final int MDEntry_Type_BID = 0;
    public static final int MDEntry_Type_OFFER = 1;
    public static final int MdQuoteType_Tradable = 1;

    @Test
    public void snapshotFullRefresh() {
        FixMessage fixMessage = new FixMessage();

        fixMessage.beginString("FIX4.4");
        fixMessage.messageType("W");
        fixMessage.addTag(FixTags.Symbol.tag, "EUR/GBP");
//        fixMessage.addTag(FixTags.MDReqId.tag, "EUR/GBP");
//        fixMessage.addTag(FixTags.NoMDEntries.tag, 2);
//        // MDEntry #1
//        fixMessage.addTag(FixTags.MDEntryType.tag, MDEntry_Type_BID);
//        fixMessage.addTag(FixTags.MDEntryPx.tag, 0.90958f);
//        fixMessage.addTag(FixTags.MDEntrySize.tag, 1000000);
//        fixMessage.addTag(FixTags.MDEntryDate.tag, 20170814);
//        fixMessage.addTag(FixTags.MDEntryTime.tag, "11:30:57.066");
//        fixMessage.addTag(FixTags.MdQuoteType.tag, MdQuoteType_Tradable);
//
//        // MDEntry #2
//        fixMessage.addTag(FixTags.MDEntryType.tag, MDEntry_Type_OFFER);
//        fixMessage.addTag(FixTags.MDEntryPx.tag, 0.90967f);
//        fixMessage.addTag(FixTags.MDEntrySize.tag, 1000000);
//        fixMessage.addTag(FixTags.MDEntryDate.tag, 20170814f);
//        fixMessage.addTag(FixTags.MDEntryTime.tag, "11:30:57.066");
//        fixMessage.addTag(FixTags.MdQuoteType.tag, MdQuoteType_Tradable);

        System.out.println(FixMessage.asString(fixMessage));

        final ByteBuffer byteBuffer = ByteBuffer.allocate(2098);
        fixMessage.writeToByteBuffer(byteBuffer);
        FixMessage newFixMessage = new FixMessage();
        byteBuffer.flip();
        SimpleFixMessageParser parser = new SimpleFixMessageParser();
        parser.parseNextMessage(byteBuffer, newFixMessage);
        System.out.println(FixMessage.asString(fixMessage));

    }

    @Test
    public void readMessageFromString() {
        FixMessage simpleFixMessage = new FixMessage();
        //String originalFixMessage = "8=FIX.4.4^A9=1332^A35=W^A34=6^A49=Server.CompID^A50=Server.SubID^A52=20170829-12:02:01.987^A56=Client.CompID^A57=Client.SubID^A55=AED/AFN^A262=AED/AFN^A7075=1504008121983^A7076=1792623784883815^A7077=1792623784886968^A7078=1792623788503463^A7079=55585^A20000=474957379^A20001=1792623784874780^A268=12^A269=0^A270=1.1^A271=1000000^A272=20170829^A273=12:02:01.983^A64=20170905^A1070=1^A278=55585/0/^A269=0^A270=1^A271=2000000^A272=20170829^A273=12:02:01.983^A64=20170905^A1070=1^A278=55585/1/^A269=0^A270=0.9^A271=5000000^A272=20170829^A273=12:02:01.983^A64=20170905^A1070=1^A278=55585/2/^A269=0^A270=0.8^A271=10000000^A272=20170829^A273=12:02:01.983^A64=20170905^A1070=1^A278=55585/3/^A269=0^A270=0.7^A271=20000000^A272=20170829^A273=12:02:01.983^A64=20170905^A1070=1^A278=55585/4/^A269=0^A270=0.6^A271=50000000^A272=20170829^A273=12:02:01.983^A64=20170905^A1070=1^A278=55585/5/^A269=1^A270=1.4^A271=1000000^A272=20170829^A273=12:02:01.983^A64=20170905^A1070=1^A278=55585/20/^A269=1^A270=1.5^A271=2000000^A272=20170829^A273=12:02:01.983^A64=20170905^A1070=1^A278=55585/21/^A269=1^A270=1.6^A271=5000000^A272=20170829^A273=12:02:01.983^A64=20170905^A1070=1^A278=55585/22/^A269=1^A270=1.7^A271=10000000^A272=20170829^A273=12:02:01.983^A64=20170905^A1070=1^A278=55585/23/^A269=1^A270=1.8^A271=20000000^A272=20170829^A273=12:02:01.983^A64=20170905^A1070=1^A278=55585/24/^A269=1^A270=1.9^A271=50000000^A272=20170829^A273=12:02:01.983^A64=20170905^A1070=1^A278=55585/25/^A10=154^A";
        String originalFixMessage = "8=FIX.4.4\u00019=857\u000135=W\u000134=1559\u000149=Server.CompID\u000150=Server.SubID\u000152=20170829-13:55:14.262\u000156=Client.CompID\u000157=Client.SubID\u000155=AED/AFN\u0001262=AED/AFN\u00017075=1504014914261\u00017076=1799416063006133\u00017077=1799416063008655\u00017078=1799416063097441\u00017079=1796016\u000120000=474957379\u000120001=1799416062997653\u0001268=12\u0001269=0\u0001270=1.2\u0001271=1000000\u0001=1796016/0/\u0001269=0\u0001270=1.1\u0001271=2000000\u0001=1796016/1/\u0001269=0\u0001270=1\u0001271=5000000\u0001=1796016/2/\u0001269=0\u0001270=0.9\u0001271=10000000\u0001=1796016/3/\u0001269=0\u0001270=0.8\u0001271=20000000\u0001=1796016/4/\u0001269=0\u0001270=0.7\u0001271=50000000\u0001=1796016/5/\u0001269=1\u0001270=1.4\u0001271=1000000\u0001=1796016/20/\u0001269=1\u0001270=1.5\u0001271=2000000\u0001=1796016/21/\u0001269=1\u0001270=1.6\u0001271=5000000\u0001=1796016/22/\u0001269=1\u0001270=1.7\u0001271=10000000\u0001=1796016/23/\u0001269=1\u0001270=1.8\u0001271=20000000\u0001=1796016/24/\u0001269=1\u0001270=1.9\u0001271=50000000\u0001=1796016/25/\u000110=004\u0001";

        SimpleFixMessageParser.parseFromFixString(originalFixMessage, "\\^A", simpleFixMessage);
        System.out.println(originalFixMessage);
        System.out.println("\n" + FixMessage.asString(simpleFixMessage));
    }

    @Test
    public void calculateCheckSum() {
        FixMessage simpleFixMessage = new FixMessage();
        String originalFixMessage = "8=FIX.4.2\u00019=180\u000135=8\u000149=Server.CompID\u000150=Server.SubId\u000156=Client.CompID\u000157=ClientSubId\u000134=300\u000152=20180220-05:30:16.961\u00016=0\u000111=1\u000114=0\u000117=execId\u000120=0\u000137=orderId\u000139=0\u000144=1\u000154=1\u000155=EUR/USD\u0001150=2\u0001151=0\u000110=201\u0001";
        SimpleFixMessageParser.parseFromFixString(originalFixMessage, "\u0001", simpleFixMessage);
        ByteBuffer byteBuffer = ByteBuffer.allocate(2094);
        simpleFixMessage.writeToByteBuffer(byteBuffer);

        //Expected CheckSum=153
        final String checkSumAsString = simpleFixMessage.checkSum.valueAsString();
        System.out.println(originalFixMessage);
        System.out.println("refCheckSum: " + calcReferenceCheckSum(originalFixMessage));
        System.out.println("\n" + FixMessage.asString(simpleFixMessage));
        Assert.assertEquals(152, (int) Integer.valueOf(checkSumAsString));

    }

    public static void main(String[] args) {
        System.out.println(calcReferenceCheckSum("8=FIX.4.2\u00019=180\u000135=8\u000149=Server.CompID\u000150=Server.SubId\u000156=Client.CompID\u000157=ClientSubId\u000134=301\u000152=20180220-05:30:16.961\u00016=0\u000111=1\u000114=0\u000117=execId\u000120=0\u000137=orderId\u000139=0\u000144=1\u000154=1\u000155=EUR/USD\u0001150=2\u0001151=0\u00011"));
    }

    public static int calcReferenceCheckSum(String string) {
        String message = string.split("10=")[0];
        long sum = 0;
        for (int i = 0; i < message.length(); i++) {
            sum += message.charAt(i);
        }
        return (int) (sum % 256);
    }

}