package smeo.experiments.playground.hibernate.model.collection;

import org.hibernate.Query;
import org.hibernate.Session;
import smeo.experiments.playground.hibernate.common.HibernateUtil;

import java.util.List;

/**
 * Created by smeo on 16.07.16.
 */
public class TestLoadCollectionsViaHQL {
    public static void main(String[] args)
    {
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();

//        CollectionEntity collectionEntity = CollectionEntity.createDefault();
//        session.save(collectionEntity);
//        session.getTransaction().commit();

        Query query = session.createQuery("from CollectionEntityWithReference where id = :id ");
        query.setParameter("id", "does not exist hql");
        List list = query.list();

        HibernateUtil.shutdown();
    }

}
