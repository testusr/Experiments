package smeo.experiments.playground.envers.model;

import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import javax.persistence.Basic;
import javax.persistence.Entity;

@Entity
@Audited
public class DerivedFromAuditedBaseEntity extends AuditedMappedSuperclass {
	@Basic
	private String attributeNotDerived;

	@NotAudited
	private String attributeMarkedAsNotAudited;

	public DerivedFromAuditedBaseEntity(String id) {
		super(id);
	}
}
