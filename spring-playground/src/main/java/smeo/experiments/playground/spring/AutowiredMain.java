package smeo.experiments.playground.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import smeo.experiments.playground.spring.model.Customer;

/**
 * Created by truehl on 05.07.16.
 */
public class AutowiredMain {
	/**
	 * With this approach i cannot load a second spring file
	 * @param args
	 */
	public static void main(String[] args) {
		ApplicationContext context = new ClassPathXmlApplicationContext(new String[] { "spring/autowired.xml" });

		System.out.println("context loaded, now requesting customer bean");
		Customer cust = (Customer) context.getBean("CustomerBean1");
		System.out.println(cust);
		System.out.println("Person is wired : " + (cust.getPerson() != null ? "YES" : "NO"));

	}
}
