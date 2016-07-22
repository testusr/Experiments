package smeo.experiments.esper.priceaggregation.domain;

/**
 * Created by truehl on 22.07.16.
 */
public interface AuditListener {
	void audit(String event, String details);
}
