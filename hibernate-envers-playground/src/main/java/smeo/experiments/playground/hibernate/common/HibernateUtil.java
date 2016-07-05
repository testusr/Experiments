package smeo.experiments.playground.hibernate.common;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.net.URL;

/**
 * Created by smeo on 09.05.16.
 */
public class HibernateUtil {
	private static SessionFactory sessionFactory = buildSessionFactory();

	private static SessionFactory buildSessionFactory() {
		try {
			if (sessionFactory == null) {
				final URL resource = HibernateUtil.class.getResource("/hibernate.cfg.xml");
				Configuration configuration = new Configuration().configure(resource);
				sessionFactory = configuration.buildSessionFactory();

				// ############ post hibernate 3.6 code ###########
				// StandardServiceRegistryBuilder serviceRegistryBuilder = new StandardServiceRegistryBuilder();
				// serviceRegistryBuilder.applySettings(configuration.getProperties());
				// ServiceRegistry serviceRegistry = serviceRegistryBuilder.build();
				// sessionFactory = configuration.buildSessionFactory(serviceRegistry);
			}
			return sessionFactory;
		} catch (Throwable ex) {
			System.err.println("Initial SessionFactory creation failed." + ex);
			throw new ExceptionInInitializerError(ex);
		}
	}

	public static SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public static void shutdown() {
		getSessionFactory().close();
	}
}