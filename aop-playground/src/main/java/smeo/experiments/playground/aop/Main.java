package smeo.experiments.playground.aop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableLoadTimeWeaving;

@SpringBootApplication
public class Main {
    public static void main(String[] args) throws Exception {
            SpringApplication.run(Main.class, args);
            TestApp.main(args);
    }
}
