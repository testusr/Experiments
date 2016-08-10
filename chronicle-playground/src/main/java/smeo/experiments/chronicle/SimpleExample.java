package smeo.experiments.chronicle;

import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder;
import net.openhft.chronicle.wire.MethodReader;
import smeo.experiments.chronicle.model.RiskMonitor;
import smeo.experiments.chronicle.model.Side;
import smeo.experiments.chronicle.model.TradeDetails;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Created by smeo on 02.08.16.
 */
public class SimpleExample {

    private static String path = "/tmp";

    public static void main(String[] args) {
        ChronicleQueue queue = SingleChronicleQueueBuilder.binary(path + "/trades").build();
        final ExcerptAppender appender = queue.acquireAppender();
        // using the method writer interface.
        RiskMonitor riskMonitor = appender.methodWriter(RiskMonitor.class);
        final LocalDateTime now = LocalDateTime.now(Clock.systemUTC());
        riskMonitor.trade(new TradeDetails(now, "GBPUSD", 1.3095, 10e6, Side.Buy, "peter"));

        ChronicleQueue readingqueue = SingleChronicleQueueBuilder.binary(path + "/trades").build();
        final ExcerptTailer tailer = readingqueue.createTailer();
        // reading using method calls
        RiskMonitor monitor = System.out::println;
        MethodReader reader = tailer.methodReader(monitor);
        // read one message
        reader.readOne();

        // dump the content of the queue
        //System.out.println(queue.dump());
    }
}
