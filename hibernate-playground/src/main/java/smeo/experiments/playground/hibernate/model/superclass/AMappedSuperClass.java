package smeo.experiments.playground.hibernate.model.superclass;

import javax.persistence.*;

/**
 * Created by truehl on 27.06.16.
 */

@MappedSuperclass
@Access(AccessType.FIELD)
public class AMappedSuperClass {

	@Id
	private String id;

	@Basic
	@Column(nullable = false, name = "MSUPER_VALUE")
	private String mappedSuperValue;

}
