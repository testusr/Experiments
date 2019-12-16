package smeo.experiments.monitoring.platform.micrometer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.util.Random;

@SpringBootApplication
public class SpringBootAppl implements CommandLineRunner {
    @Autowired
    TestServiceA testServiceA;

    public static void main(String[] args) {
        SpringApplication.run(SpringBootAppl.class, args);
    }


    @Override
    public void run(String... args) throws Exception {
    //    testServiceA.init();
        Random random = new Random();
        while (true) {
            switch (String.valueOf(random.nextInt(6))) {
               /* case "1":
                    testServiceA.method();
                    break;
                case "2":
                    testServiceA.method(random.nextDouble());
                    break;
                case "3":
                    testServiceA.method(String.valueOf(random.nextDouble()));
                    break;*/
                case "4":
                    testServiceA.event();
                    break;
                case "5":
                    testServiceA.event2();
                    break;
                default:
            }
            ;
        }
    }
}
