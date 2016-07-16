package smeo.experiments.playground.hibernate.model.collection;

import org.hibernate.Session;
import smeo.experiments.playground.hibernate.common.HibernateUtil;
import smeo.experiments.playground.hibernate.model.EmployeeEntity;

/**
 * Created by smeo on 16.07.16.
 */
public class TestLoadCollectionsViaSession {
    public static void main(String[] args)
    {
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();

//        CollectionEntity collectionEntity = CollectionEntity.createDefault();
//        session.save(collectionEntity);
//        session.getTransaction().commit();

        session.get(CollectionEntityWithReference.class, "does not exist");
        HibernateUtil.shutdown();
    }

}
