package smeo.experiments.playground.spring;

/**
 * Created by smeo on 06.08.16.
 */
public class LazyLoadingLoaderMainWoLazyReader {
    public static void main(String[] args) {
        LazyBeanLoader lazyBeanLoader = new LazyBeanLoader(true);
        String PATH = "/home/smeo/repository/Experiments/spring-playground/src/main/resources/";
        lazyBeanLoader.loadBeanDefinitions(PATH+"spring/autowired.xml");
      //  lazyBeanLoader.getBean("CustomerBean1");

        System.out.println("loading second spring file");

        lazyBeanLoader.loadBeanDefinitions(PATH+"spring/secondBeanSet.xml");

        System.out.println("context loaded, now requesting customer bean");
        lazyBeanLoader.getBean("CustomerBean1");


    }
}
