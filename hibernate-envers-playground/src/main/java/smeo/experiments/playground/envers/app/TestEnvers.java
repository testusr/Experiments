package smeo.experiments.playground.envers.app;

import org.hibernate.Session;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import smeo.experiments.playground.envers.common.HibernateUtil;
import smeo.experiments.playground.envers.model.Address;
import smeo.experiments.playground.envers.model.EmbeddedPosition;
import smeo.experiments.playground.envers.model.EmployeeEntity;
import smeo.experiments.playground.envers.revisionentity.RevisionEntity;

import java.util.List;

/**
 * Created by smeo on 09.05.16.
 * http://howtodoinjava.com/hibernate/hibernate-4-using-in-memory-database-with-hibernate/
 */
public class TestEnvers {

	public static void testFetchingOldObjectVersion(Session session) {
		AuditReader auditReader = AuditReaderFactory.get(session);

		List<Object[]> listOfVersions = auditReader.createQuery()
				.forRevisionsOfEntity(EmployeeEntity.class, false, true)
				.getResultList();
		System.out.println("#############################################");
		System.out.println("## LIST OF VERSIONS:");

		int lastVersion = 1;
		for (Object[] curreEntrySet : listOfVersions) {
			lastVersion = ((RevisionEntity) curreEntrySet[1]).getId();
			System.out.printf("## RevisionType: %s Revision: %d\n", String.valueOf(curreEntrySet[2]),
					lastVersion);
		}

		System.out.println("###############################################");
		for (int i = 1; i <= lastVersion; i++) {
			EmployeeEntity historicalEmployVersion = (EmployeeEntity) auditReader.createQuery()
					.forEntitiesAtRevision(EmployeeEntity.class, i)
					.getSingleResult();
			System.out.printf("## Historical v%d: %s \n", i, historicalEmployVersion.toString());
		}
	}

	/**
	 * Shows how attribute changes are recorded in the AUD table
	 */
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

	/**
	 * Shows nicely that every change on any object causes an increase of a global version
	 * 
	 * @param session
	 */
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

	public static void main(String[] args) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		testAttributeChanges(session);
		// testChangesToReferencedObject(session);
		// testChangeAttributesOfDifferentObjects(session);
		// testChangeToEmbbededObjects(session);
		// testChangeToListOfEmbbededObjects(session);
		testFetchingOldObjectVersion(session);

		HibernateUtil.shutdown();
	}

	private static void testChangeToListOfEmbbededObjects(Session session) {
		session.beginTransaction();
		EmployeeEntity emp = defaultEmployee();
		save(session, emp);

		session.beginTransaction();
		emp.addSubPosition(new EmbeddedPosition("pos1", 11));
		save(session, emp);

		session.beginTransaction();
		emp.addSubPosition(new EmbeddedPosition("pos2", 22));
		save(session, emp);

		session.beginTransaction();
		emp.addSubPosition(new EmbeddedPosition("pos3", 33));
		save(session, emp);

	}

	/**
	 * Treated like regular attribute changes increase version by one
	 */
	private static void testChangeToEmbbededObjects(Session session) {
		session.beginTransaction();
		EmployeeEntity emp = defaultEmployee();
		save(session, emp);

		session.beginTransaction();
		emp.setMainPosition(new EmbeddedPosition("pos1", 11));
		save(session, emp);

		session.beginTransaction();
		emp.setMainPosition(new EmbeddedPosition("pos2", 22));
		save(session, emp);

		session.beginTransaction();
		emp.setMainPosition(new EmbeddedPosition("pos3", 33));
		save(session, emp);

	}
}
