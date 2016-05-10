package smeo.experiments.playground.envers.revisionentity;

import org.hibernate.envers.RevisionListener;

/**
 * Listener that is automatically called by envers on every change.
 * A new RevisionEntity instance is passed to the listener to be enriched with
 * meta information for every change.
 * 
 */
public class RevisionListenerImpl implements RevisionListener {
	@Override
	public void newRevision(Object o) {
		RevisionEntity revisionEntity = (RevisionEntity) o;
		revisionEntity.setChangeOriginator("truehl");
	}
}
