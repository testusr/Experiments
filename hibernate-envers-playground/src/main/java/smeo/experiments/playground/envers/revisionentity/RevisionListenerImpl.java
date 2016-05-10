package smeo.experiments.playground.envers.revisionentity;

import org.hibernate.envers.RevisionListener;

/**
 * Created by truehl on 10.05.16.
 */
public class RevisionListenerImpl implements RevisionListener {
	@Override
	public void newRevision(Object o) {
		RevisionEntity revisionEntity = (RevisionEntity) o;
		revisionEntity.setChangeOriginator("truehl");
	}
}
