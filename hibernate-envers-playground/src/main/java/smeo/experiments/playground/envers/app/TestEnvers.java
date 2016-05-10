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
	public static void testAttributeChanges(Session session) {
		session.beginTransaction();
		// Add new Employee object
		EmployeeEntity emp = defaultEmployee();
		save(session, emp);

		session.beginTransaction();
		emp.setFirstName("change#1");
		save(session, emp);

		session.beginTransaction();
		emp.setLastName("change#2");
		save(session, emp);

		session.beginTransaction();
		emp.setFirstName("change#3");
		emp.setLastName("change#3");
		emp.setEmail("change#3");
		save(session, emp);

	}

	private static EmployeeEntity defaultEmployee() {
		EmployeeEntity emp = new EmployeeEntity();
		emp.setEmployeeId(1);
		emp.setEmail("default@mail.com");
		emp.setFirstName("default-firstname");
		emp.setLastName("default-lastName");
		return emp;
	}

	public static void testChangesToReferencedObject(Session session) {
		session.beginTransaction();
		// Add new Employee object
		EmployeeEntity emp = defaultEmployee();
		save(session, emp);

		session.beginTransaction();
		Address address = new Address("id#a1");
		address.setPostcode(1111);
		address.setStreet("first marvel street");
		address.setHouseNo(11);

		session.save(address);
		emp.setAdress(address);
		save(session, emp);

		session.beginTransaction();
		address.setPostcode(2222);
		address.setStreet("second dc street");
		address.setHouseNo(22);
		save(session, emp);
	}

	public static void main(String[] args) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		// testAttributeChanges(session);
		// testChangesToReferencedObject(session);
		testChangeAttributesOfDifferentObjects(session);

		HibernateUtil.shutdown();
	}

	private static void testChangeAttributesOfDifferentObjects(Session session) {
		session.beginTransaction();
		// Add new Employee object
		EmployeeEntity emp = defaultEmployee();

		Address address = new Address("id#a1");
		address.setPostcode(1111);
		address.setStreet("first marvel street");
		address.setHouseNo(11);
		emp.setAdress(address);
		session.save(address);
		save(session, emp);

		session.beginTransaction();
		address.setPostcode(2222);
		save(session, address);

		session.beginTransaction();
		emp.setFirstName("3333");
		save(session, emp);

		session.beginTransaction();
		address.setPostcode(4444);
		save(session, address);

		session.beginTransaction();
		emp.setFirstName("5555");
		save(session, emp);

		session.beginTransaction();
		address.setPostcode(6666);
		save(session, address);

		session.beginTransaction();
		emp.setFirstName("7777");
		save(session, emp);

		session.beginTransaction();
		address.setPostcode(8888);
		emp.setFirstName("8888");
		save(session, emp);

		session.beginTransaction();
		address.setPostcode(9999);
		emp.setFirstName("9999");
		save(session, emp);

	}

	private static void save(Session session, Object toPersist) {
		session.save(toPersist);
		session.getTransaction().commit();
	}
}
