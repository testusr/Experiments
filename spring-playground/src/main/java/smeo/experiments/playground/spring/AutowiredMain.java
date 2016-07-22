package smeo.experiments.playground.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import smeo.experiments.playground.spring.model.Customer;

/**
 * Created by truehl on 05.07.16.
 */
public class AutowiredMain {
	public static void main(String[] args) {
		ApplicationContext context = new ClassPathXmlApplicationContext(new String[] { "spring/autowired.xml" });

		Customer cust = (Customer) context.getBean("CustomerBean");
		System.out.println(cust);

	}
}
