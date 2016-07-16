package smeo.experiments.playground.hibernate.model.collection;

import smeo.experiments.playground.hibernate.model.ValueEntity;

import javax.persistence.*;
import java.util.Collection;
import java.util.UUID;

/**
 * Created by smeo on 16.07.16.
 */
@Entity
public class CollectionEntity {
    @Id
    private String id;

    @ElementCollection(fetch = FetchType.EAGER)
    Collection<ValueEntity> eagerLoadedEntityCollection;

    @ElementCollection(fetch = FetchType.LAZY)
    Collection<ValueEntity> lazyLoadedEntityCollection;

    private CollectionEntity(String id) {
        this.id = id;
    }

    private CollectionEntity(){}

    public static CollectionEntity createDefault(){
        String uuid = UUID.randomUUID().toString();
        CollectionEntity collectionEntity = new CollectionEntity(uuid);
        for (int i=0; i < 3; i++) {
            collectionEntity.eagerLoadedEntityCollection.add(new ValueEntity(uuid+i, "eager#" + String.valueOf(i)));
            collectionEntity.lazyLoadedEntityCollection.add(new ValueEntity(uuid+i, "lazy#" + String.valueOf(i)));
        }
        return collectionEntity;
    }

}
