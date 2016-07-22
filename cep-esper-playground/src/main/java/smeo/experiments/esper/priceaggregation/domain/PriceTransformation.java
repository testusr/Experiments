package smeo.experiments.esper.priceaggregation.domain;

import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import com.espertech.esper.event.map.MapEventBean;
import org.apache.commons.lang3.Validate;
import smeo.experiments.utils.jmx.MBeanBuilder;
import smeo.experiments.utils.jmx.Managed;

import javax.management.ObjectName;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by truehl on 22.07.16.
 */
public class PriceTransformation implements UpdateListener {
	private final String name;
	private final EPServiceProvider epService;
	private final AuditListener auditListener;

	private String eslExpression;
	private EPStatement statement;

	public PriceTransformation(String name, EPServiceProvider epService, AuditListener auditListener) {
		this.name = name;
		this.epService = epService;
		this.auditListener = auditListener;
		registerMBean();
	}

	@Managed
	public void start() {
		Validate.isTrue(!isStarted(), "transformation was already stared");
		if (!isStarted()) {
			if (statement == null) {
				statement = epService.getEPAdministrator()
						.createEPL(eslExpression);
			} else {
				statement.start();
			}
			audit("started");
		}
	}

	private void audit(String event) {
		if (auditListener != null) {
			auditListener.audit("transformation[" + event + "]", name);
		}
	}

	@Managed
	public void stop() {
		if (isStarted()) {
			statement.stop();
			audit("stopped");
		}
	}

	@Managed
	public boolean isStarted() {
		if (statement != null) {
			return statement.isStarted();
		}
		return false;
	}

	@Managed
	public void replaceTransformation(String newExpression) {
		if (isStarted()) {
			stop();
		}
		if (statement != null) {
			statement.removeAllListeners();
			statement.destroy();
		}

		String oldEslExpression = this.eslExpression;
		this.eslExpression = newExpression;
		statement = epService.getEPAdministrator().createEPL(eslExpression);
		statement.addListener(this);
		audit("replaced['" + oldEslExpression + "' -> '" + eslExpression + "']");
	}

	@Managed
	public String getName() {
		return name;
	}

	@Managed
	public String getEslExpression() {
		return eslExpression;
	}

	public void update(EventBean[] newEvents, EventBean[] oldEvents) {
		EventBean event = newEvents[0];
		if (event instanceof MapEventBean) {
			final MapEventBean mappedEvent = (MapEventBean) event;

			final Iterator<Map.Entry<String, Object>> iProperties = mappedEvent.getProperties()
					.entrySet()
					.iterator();

			StringBuilder eventValues = new StringBuilder();
			eventValues.append("Event[").append(name).append("]:\n");
			while (iProperties.hasNext()) {
				final Map.Entry<String, Object> next = iProperties.next();
				eventValues.append(next.getKey()).append("= ").append(next.getValue()).append("\n");
			}
			eventValues.append("--\n");
			System.out.println(eventValues.toString());

		}
	}

	private void registerMBean() {
		try {
			ObjectName objectNameWithSubfolderHierarchy = MBeanBuilder.createObjectNameWithSubfolderHierarchy(getClass().getSimpleName() + "-" + hashCode(),
					"smeo.playground", "esper", "transformation", getName());
			MBeanBuilder.generateAndRegisterMBean(this, objectNameWithSubfolderHierarchy, false);
		} catch (Throwable e) {

		}
	}
}
