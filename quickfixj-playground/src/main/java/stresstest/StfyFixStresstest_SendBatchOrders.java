package stresstest;

import com.opencsv.CSVWriter;
import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import lombok.Getter;
import lombok.Setter;
import quickfix.*;
import quickfix.fix44.ExecutionReport;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class StfyFixStresstest_SendBatchOrders extends AbstractFixTestClient {
    public static final int ORDER_BATCH_SIZE = 7000;
    private static final String FIX_ORDER_SINGLE_TEMPLATE = "";
    List<OrderRecord> placedOrderRecords;
    String accountTemplate;
    private int orderPerAccount = 5;
    private int noOfAccounts = 100;
    private int noOfInstruments = 60;
    private DefaultMessageFactory messageFactory = new DefaultMessageFactory();


    @Override
    void runtest(Properties testproperties) {
        placedOrderRecords = prepareOrders(ORDER_BATCH_SIZE);
        for (int i = 0; i < placedOrderRecords.size(); i++){
            try {
                placeOrder(placedOrderRecords.get(i));
            } catch (InvalidMessage invalidMessage) {
                throw new RuntimeException(invalidMessage);
            }
        }
        waitForAllOrderResponses(TimeUnit.SECONDS.toMillis(30));
        writeCsvReport(placedOrderRecords, "/tmp/stfy-fixstresstest-orderbatches-timings.csv");
    }

    @Handler
    void handleOrderResponse(ExecutionReport executionReport, SessionID sessionID) throws FieldNotFound {
        boolean wasRejected = executionReport.getExecType().getValue() == '8';
        long transactionTimeMillis = executionReport.getTransactTime().getValue().toInstant(ZoneOffset.UTC).toEpochMilli();
        String orderId = executionReport.getOrderID().toString();

        OrderRecord orderRecord = getTimingRecordById(orderId);
        if (orderRecord != null) {
            orderRecord.reportReceived(wasRejected, transactionTimeMillis);
        } else {
            System.err.println("could not find order record for order '"+orderId+"'");
        }
    }


    private List<OrderRecord> prepareOrders(int orderBatchSize) {
        List<OrderRecord> records = new ArrayList<>();
        for (int i = 0; i < orderBatchSize; i++) {
            OrderRecord newRecord = new OrderRecord();
            newRecord.accountId = String.format(accountTemplate, (i % orderPerAccount) % noOfAccounts);
            newRecord.instrumentId = String.format(accountTemplate, (i % noOfInstruments));
            newRecord.isBuy = (i%2 == 0);
        }
        return records;
    }



    private OrderRecord getTimingRecordById(String orderIdToString) {
        for (int i=0; i < placedOrderRecords.size(); i++){
            if (placedOrderRecords.get(i).orderId.equals(orderIdToString)){
                return placedOrderRecords.get(i);
            }
        }
        return null;
    }

    private void writeCsvReport(List<OrderRecord> placedOrderRecords, String filename) {
        Writer writer  = null;
        try {
            writer = new FileWriter(filename);

        StatefulBeanToCsv sbc = new StatefulBeanToCsvBuilder(writer)
                .withSeparator(CSVWriter.DEFAULT_SEPARATOR)
                .build();

        sbc.write(placedOrderRecords);
        writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private OrderRecord placeOrder(OrderRecord orderRecord) throws InvalidMessage {
        orderRecord.orderPlacedTime = System.currentTimeMillis();
        String fixMessage = String.format(FIX_ORDER_SINGLE_TEMPLATE, orderRecord.orderId, orderRecord.accountId, orderRecord.instrumentId, fixTime(orderRecord.orderPlacedTime));
        MessageUtils.parse(messageFactory, null, fixMessage);

        return null;
    }

    private String fixTime(long orderPlacedTime) {
        return null;
    }

    private void waitForAllOrderResponses(long timeoutInMillis) {
        long timeout = System.currentTimeMillis() + timeoutInMillis;
        while (System.currentTimeMillis() < timeout){
            for (int i = 0; i < placedOrderRecords.size(); i++) {
                if (!placedOrderRecords.get(i).resultReceived()){
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
        throw new IllegalArgumentException("TIMEOUT while waiting for all responses");
    }


    @Getter
    @Setter
    private class OrderRecord {
        @CsvBindByPosition(position = 0)
        String orderId;
        @CsvBindByPosition(position = 1)
        String orderInstrument;
        @CsvBindByPosition(position = 2)
        String accountId;
        @CsvBindByPosition(position = 3)
        String instrumentId;
        @CsvBindByPosition(position = 4)
        boolean isBuy;
        @CsvBindByPosition(position = 5)
        long orderPlacedTime;
        @CsvBindByPosition(position = 6)
        long orderExecutedTime;
        @CsvBindByPosition(position = 7)
        long resultReceivedTime = -1;
        @CsvBindByPosition(position = 8)
        boolean orderExecuted;

        boolean resultReceived(){
            return resultReceivedTime > 0;
        }

        public void reportReceived(boolean wasRejected, long transactionTimeMillis) {
            this.orderExecuted = !wasRejected;
            this.orderExecutedTime = transactionTimeMillis;
            this.resultReceivedTime = System.currentTimeMillis();
        }
    }

}
