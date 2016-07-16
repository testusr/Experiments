package smeo.experiments.playground.hibernate.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Created by smeo on 16.07.16.
 */
@Entity
public class ValueEntity {
    @Id
    private String id;
    private String value;

    private ValueEntity(){}

    public ValueEntity(String id, String value) {
        this.id = id;
        this.value = value;
    }
}
