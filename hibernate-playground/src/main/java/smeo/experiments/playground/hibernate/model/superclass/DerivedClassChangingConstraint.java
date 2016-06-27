package smeo.experiments.playground.hibernate.model.superclass;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * The class is deriving from the superclass and changing the
 */
@Entity
@Table(name = "DerivedClassA")
@AttributeOverride(name = "mappedSuperValue", column = @Column(nullable = false, name = "MSUPER_VALUE", unique = true))
public class DerivedClassChangingConstraint extends AMappedSuperClass {

}
