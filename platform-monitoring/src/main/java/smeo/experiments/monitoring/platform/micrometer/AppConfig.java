package smeo.experiments.monitoring.platform.micrometer;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.influx.InfluxConfig;
import io.micrometer.influx.InfluxMeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.time.Duration;

@Configuration
@ComponentScan("smeo.experiments.monitoring.platform.micrometer")
@EnableAspectJAutoProxy
public class AppConfig {
    @Bean
    public MeterRegistry getRegistry() {

        InfluxConfig config = new InfluxConfig() {
            @Override
            public Duration step() {
                return Duration.ofSeconds(1);
            }

            @Override
            public String db() {
                return "micrometer";
            }

            @Override
            public String get(String s) {
                return null;
            }
        };
        return new InfluxMeterRegistry(config, Clock.SYSTEM);
    }

    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
}
