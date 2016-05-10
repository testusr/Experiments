package smeo.experiments.playground.envers.revisionentity;

import org.hibernate.envers.DefaultRevisionEntity;

import javax.persistence.Entity;

/**
 * Created by truehl on 10.05.16.
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
