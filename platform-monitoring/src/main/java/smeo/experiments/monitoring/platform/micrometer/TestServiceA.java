package smeo.experiments.monitoring.platform.micrometer;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.LongTaskTimer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;


@Service
public class TestServiceA {

    MeterRegistry registry;


    @Autowired
    public void setRegistry(MeterRegistry registry) {
        this.registry = registry;
        System.out.println("registry set '"+registry+"'");
        Metrics.addRegistry(registry);
        new JvmMemoryMetrics().bindTo(registry);
        new JvmGcMetrics().bindTo(registry);
        new JvmThreadMetrics().bindTo(registry);
    }


    public void method(){
        System.out.println("method");
        Metrics.counter("TestServiceA.method", "tag", "1").increment();
        registry.timer("timer-1", "tag", "1").record(
                ()-> {
                    try {
                        Thread.sleep(3L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
        );
    }
    public void method(double val){
        System.out.println("method2");
        Metrics.counter("TestServiceA.method-1", "tag", "2").increment();
        registry.timer("timer-1", "tag", "2").record(
                ()-> {
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
        );

    }
    public void method(String val){
        System.out.println("method3");
        Metrics.counter("TestServiceA.method-2", "tag", "3").increment();
        LongTaskTimer longTaskTimer = LongTaskTimer
                .builder("3rdPartyService")
                .register(registry);

        LongTaskTimer.Sample currentTaskId = longTaskTimer.start();
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException ignored) { }
        currentTaskId.stop();

    }

    @Timed(value = "service.method4")
    public void event(){
        System.out.println("method4");
    }


    @Timed(value = "service.method5", extraTags = {"type", "foo"} )
    public void event2(){
        System.out.println("method5");
    }

}
