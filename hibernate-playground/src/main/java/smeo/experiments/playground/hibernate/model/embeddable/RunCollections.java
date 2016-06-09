package smeo.experiments.playground.hibernate.model.embeddable;

import org.hibernate.Session;
import smeo.experiments.playground.hibernate.common.HibernateUtil;

/**
 * Created by truehl on 09.06.16.
 */
public class RunCollections {
	public static void main(String[] args) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();
		// Add new Employee object

		final CollectionOfEmbeddables collectionOfEmbeddables = CollectionOfEmbeddables.create();
		collectionOfEmbeddables.institutions.add(Institution.create("InstitutionA"));
		collectionOfEmbeddables.institutions.add(Institution.create("InstitutionB"));
		collectionOfEmbeddables.institutions.add(Institution.create("InstitutionC"));

		session.save(collectionOfEmbeddables);
		session.getTransaction().commit();

		session.beginTransaction();

		collectionOfEmbeddables.institutions.add(Institution.create("InstitutionD"));
		collectionOfEmbeddables.institutions.add(Institution.create("InstitutionE"));
		collectionOfEmbeddables.institutions.add(Institution.create("InstitutionF"));

		session.save(collectionOfEmbeddables);
		session.getTransaction().commit();

		HibernateUtil.shutdown();
	}
}
