package smeo.experiments.playground.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import smeo.experiments.playground.spring.model.Customer;

/**
 * Created by smeo on 06.08.16.
 */
public class LazyLoadingLoaderMain {
    public static void main(String[] args) {
        LazyBeanLoader lazyBeanLoader = new LazyBeanLoader();
        String PATH = "/home/smeo/repository/Experiments/spring-playground/src/main/resources/";
        lazyBeanLoader.loadBeanDefinitions(PATH+"spring/autowired.xml");
        System.out.println("requesting CustomerBean1");
        lazyBeanLoader.getBean("CustomerBean1");

        System.out.println("loading second spring file");

        lazyBeanLoader.loadBeanDefinitions(PATH+"spring/secondBeanSet.xml");

        System.out.println("context loaded, now requesting customer bean");
        System.out.println("requesting CustomerBean2");
        lazyBeanLoader.getBean("CustomerBean2");


    }
}
