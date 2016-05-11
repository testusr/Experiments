package smeo.experiments.playground.envers.app;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.proxy.HibernateProxy;
import smeo.experiments.playground.envers.common.HibernateUtil;
import smeo.experiments.playground.envers.common.XmlUtils;
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
	private static Logger LOGGER = Logger.getLogger(TestEnvers.class);

	public static void testRoleBackOfSingleObjectWithReferences(Session session) {
		// create object with references and changes to all objects
		final int ENTITY_ID = 3;
		EmployeeEntity latestStateBeforeRoleBack = getLatesEmployeeEntityWithId(ENTITY_ID, session);

		final int ROLE_BACK_TO_VERSION = 1;
		roleBack(EmployeeEntity.class, ENTITY_ID, ROLE_BACK_TO_VERSION, session);
		EmployeeEntity latestStateAfterRoleBack = getLatesEmployeeEntityWithId(ENTITY_ID, session);
	}

	private static void roleBack(Class<?> type, int id, int toRevision, Session session) {
		session.clear();
		AuditReader auditReader = AuditReaderFactory.get(session);

		EmployeeEntity historicalEmployVersion = getEntryInRevisionViaListIteration(EmployeeEntity.class, 1, session);
		session.beginTransaction();
		printLog(historicalEmployVersion.getAdress());
		printLog(historicalEmployVersion);
		merge(session, historicalEmployVersion);
	}

	private static void printLog(Object toPrint) {
		LOGGER.info(XmlUtils.objectToXml(toPrint));
	}

	public static <T> T initializeAndUnproxy(T entity) {
		if (entity == null) {
			throw new NullPointerException("Entity passed for initialization is null");
		}

		Hibernate.initialize(entity);
		if (entity instanceof HibernateProxy) {
			entity = (T) ((HibernateProxy) entity).getHibernateLazyInitializer()
					.getImplementation();
		}
		return entity;
	}

	private static EmployeeEntity getLatesEmployeeEntityWithId(int id, Session session) {
		StringBuilder hql = new StringBuilder();
		hql.append("FROM ").append(EmployeeEntity.class.getSimpleName())
				.append(" WHERE id = :employee_id");

		Query byIdQuery = session.createQuery(hql.toString());
		byIdQuery.setParameter("employee_id", id);

		List list = byIdQuery.list();
		if (!list.isEmpty()) {
			return (EmployeeEntity) list.get(0);
		}

		return null;
	}

	public static <T> T getEntryInRevisionViaDirectQuery(Class<T> clazz, int id, int revision, Session session) {
		AuditReader auditReader = AuditReaderFactory.get(session);
		final List resultList = auditReader.createQuery()
				.forEntitiesAtRevision(EmployeeEntity.class, revision)
				.add(AuditEntity.id().eq(id))
				.getResultList();

		if (resultList.size() != 1) {
			throw new RuntimeException("found '" + resultList.size() + "' no of results for object to revert, this should be exactly one");
		}

		return (T) resultList.get(0);
	}

	public static <T> T getEntryInRevisionViaListIteration(Class<T> clazz, int revision, Session session) {
		AuditReader auditReader = AuditReaderFactory.get(session);
		List<Object[]> listOfVersions = auditReader.createQuery()
				.forRevisionsOfEntity(clazz, false, true)
				.add(AuditEntity.revisionNumber().eq(revision))
				.getResultList();

		for (Object[] curreEntrySet : listOfVersions) {

			if (revision == ((RevisionEntity) curreEntrySet[1]).getId()) {
				return (T) curreEntrySet[0];
			}
		}
		return null;
	}

	public static void testFetchingOldObjectVersion(Session session) {
		AuditReader auditReader = AuditReaderFactory.get(session);

		List<Object[]> listOfVersions = auditReader.createQuery()
				.forRevisionsOfEntity(EmployeeEntity.class, false, true)
				.getResultList();
		StringBuilder message = new StringBuilder("#############################################\n");
		message.append("## LIST OF VERSIONS:\n");

		int lastVersion = 1;
		for (Object[] curreEntrySet : listOfVersions) {

			lastVersion = ((RevisionEntity) curreEntrySet[1]).getId();
			message.append(String.format("## RevisionType: %s Revision: %d postcode: %d\n", String.valueOf(curreEntrySet[2]), lastVersion,
					((EmployeeEntity) curreEntrySet[0]).getAdress()
							.getPostcode()));

			message.append("###############################################\n");
			for (int i = 1; i <= lastVersion; i++) {
				EmployeeEntity historicalEmployVersion = (EmployeeEntity) auditReader.createQuery()
						.forEntitiesAtRevision(EmployeeEntity.class, i)
						.getSingleResult();
				message.append(String.format("## Historical v%d: %s \n", i, historicalEmployVersion.toString()));
			}

		}
		LOGGER.info(message.toString());
	}

	/**
	 * Shows how attribute changes are recorded in the AUD table
	 */
	public static void testAttributeChanges(Session session) {
		session.beginTransaction();
		// Add new Employee object
		EmployeeEntity emp = defaultEmployee(1);
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

	private static EmployeeEntity defaultEmployee(int id) {
		EmployeeEntity emp = new EmployeeEntity();
		emp.setEmployeeId(id);
		emp.setEmail("default@mail.com");
		emp.setFirstName("default-firstname");
		emp.setLastName("default-lastName");
		return emp;
	}

	public static void testChangesToReferencedObject(Session session) {
		session.beginTransaction();
		EmployeeEntity emp = defaultEmployee(2);
		save(session, emp);

		session.beginTransaction();
		Address address = new Address("id#a2");
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
		EmployeeEntity emp = defaultEmployee(3);

		Address address = new Address("id#a3");
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

	private static void merge(Session session, Object... toPersist) {
		for (int i = 0; i < toPersist.length; i++) {
			session.merge(toPersist[i]);
		}
		session.getTransaction().commit();
	}

	private static void saveOrUpdate(Session session, Object... toPersist) {
		for (int i = 0; i < toPersist.length; i++) {
			session.saveOrUpdate(toPersist[i]);
		}
		session.getTransaction().commit();
	}

	private static void save(Session session, Object... toPersist) {
		for (int i = 0; i < toPersist.length; i++) {
			session.save(toPersist[i]);
		}
		session.getTransaction().commit();
	}

	private static void testChangeToListOfEmbbededObjects(Session session) {
		session.beginTransaction();
		EmployeeEntity emp = defaultEmployee(4);
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
		EmployeeEntity emp = defaultEmployee(5);
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

	public static void main(String[] args) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		// testAttributeChanges(session);
		// testChangesToReferencedObject(session);
		testChangeAttributesOfDifferentObjects(session);
		session.close();

		// testChangeToEmbbededObjects(session);
		// testChangeToListOfEmbbededObjects(session);
		// testFetchingOldObjectVersion(session);
		session = HibernateUtil.getSessionFactory().openSession();
		testRoleBackOfSingleObjectWithReferences(session);

		HibernateUtil.shutdown();
	}
}
