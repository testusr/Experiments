package smeo.experiments.playground.envers.app;

import org.hibernate.Session;
import smeo.experiments.playground.envers.common.HibernateUtil;
import smeo.experiments.playground.envers.model.Address;
import smeo.experiments.playground.envers.model.EmployeeEntity;

/**
 * Created by smeo on 09.05.16.
 * http://howtodoinjava.com/hibernate/hibernate-4-using-in-memory-database-with-hibernate/
 */
public class TestEnvers {
	public static void main(String[] args) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();
		// Add new Employee object
		EmployeeEntity emp = new EmployeeEntity();
		emp.setEmployeeId(1);
		emp.setEmail("demo-user@mail.com");
		emp.setFirstName("demo");
		emp.setLastName("user");
		save(session, emp);

		session.beginTransaction();
		emp.setFirstName("change#1");
		save(session, emp);

		session.beginTransaction();
		Address address = new Address("id#a1");
		address.setPostcode(1234);
		address.setStreet("mable street");
		address.setHouseNo(11);

		session.save(address);
		emp.setAdress(address);
		save(session, emp);

		session.beginTransaction();
		address.setStreet("change#2");
		save(session, emp);

		// session.beginTransaction();
		// emp.setEmail("demo-user@mail.com");
		// emp.setFirstName("demo");
		// emp.setLastName("change2#user");
		// save(session, emp);
		//
		// session.beginTransaction();
		// emp.setEmail("dcahnge3#emo-user@mail.com");
		// emp.setFirstName("demo");
		// emp.setLastName("user");
		// save(session, emp);

		HibernateUtil.shutdown();
	}

	private static void save(Session session, EmployeeEntity emp) {
		session.save(emp);
		session.getTransaction().commit();
	}
}
