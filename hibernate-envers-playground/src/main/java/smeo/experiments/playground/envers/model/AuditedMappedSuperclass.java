package smeo.experiments.playground.envers.model;

import org.hibernate.envers.Audited;

import javax.persistence.*;

/**
 * Created by truehl on 10.05.16.
 */
@Audited
@org.hibernate.annotations.Entity(dynamicUpdate = true)
@MappedSuperclass
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class AuditedMappedSuperclass {
	@Id
	@Column(name = "ID", unique = true, nullable = false)
	private final String id;

	public AuditedMappedSuperclass(String id) {
		this.id = id;
	}
}
