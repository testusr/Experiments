package smeo.experiments.playground.hibernate.revisionentity;

import org.hibernate.envers.DefaultRevisionEntity;

import javax.persistence.Entity;

/**
 * RevisionEntity is used to add additional information to every change that is audited.
 * The object is automatically created on each change and passed to a listener object
 * specified via @RevisionEntity
 */
@Entity
@org.hibernate.envers.RevisionEntity(RevisionListenerImpl.class)
public class RevisionEntity extends DefaultRevisionEntity {
	private String changeOriginator;

	public void setChangeOriginator(String changeOriginator) {
		this.changeOriginator = changeOriginator;
	}

	public String getChangeOriginator() {
		return changeOriginator;
	}
}
