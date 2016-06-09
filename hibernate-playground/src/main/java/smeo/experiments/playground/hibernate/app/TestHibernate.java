package smeo.experiments.playground.hibernate.app;

import org.hibernate.Session;
import smeo.experiments.playground.hibernate.common.HibernateUtil;
import smeo.experiments.playground.hibernate.model.EmployeeEntity;

/**
 * Created by smeo on 09.05.16.
 * http://howtodoinjava.com/hibernate/hibernate-4-using-in-memory-database-with-hibernate/
 */
public class TestHibernate {
    public static void main(String[] args)
    {
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        // Add new Employee object
        EmployeeEntity emp = new EmployeeEntity();
        emp.setEmployeeId(1);
        emp.setEmail("demo-user@mail.com");
        emp.setFirstName("demo");
        emp.setLastName("user");
        session.save(emp);
        session.getTransaction().commit();
        HibernateUtil.shutdown();
    }
}
