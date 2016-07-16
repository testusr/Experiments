package smeo.experiments.playground.hibernate.model.collection;

import smeo.experiments.playground.hibernate.model.ValueEntity;

import javax.persistence.*;
import java.util.Collection;

/**
 * Created by smeo on 16.07.16.
 */

@Entity
@Access(AccessType.FIELD)
public class CollectionEntityWithReference {
    @Id
    private String id;

    @ElementCollection(fetch = FetchType.EAGER)
    Collection<ValueEntity> ref_eagerLoadedEntityCollection;

    @OneToOne
    private CollectionEntity referencedCollectionEntity;

    private CollectionEntityWithReference(){};

    public CollectionEntityWithReference(String id) {
        this.id = id;
    }
}
