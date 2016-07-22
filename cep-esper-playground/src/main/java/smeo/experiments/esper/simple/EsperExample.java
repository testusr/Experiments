package smeo.experiments.esper.simple;

import com.espertech.esper.client.*;
import smeo.experiments.esper.simple.event.OrderEvent;

/**
 * Created by truehl on 22.07.16.
 */
public class EsperExample {
	public static void main(String[] args) {
		Configuration config = new Configuration();
		config.addEventTypeAutoName("smeo.experiments.esper.simple.event");

		EPServiceProvider epService = EPServiceProviderManager.getDefaultProvider(config);
		String expression = "select avg(price) from OrderEvent.win:time(30 sec)";
		EPStatement statement = epService.getEPAdministrator().createEPL(expression);

		MyListener listener = new MyListener();
		statement.addListener(listener);

		generateEvents(epService);
	}

	private static void generateEvents(EPServiceProvider epService) {
		int i = 1;
		while (true) {
			OrderEvent event = new OrderEvent("shirt", i);
			epService.getEPRuntime().sendEvent(event);
			i++;
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static class MyListener implements UpdateListener {
		public void update(EventBean[] newEvents, EventBean[] oldEvents) {
			EventBean event = newEvents[0];
			System.out.println("avg=" + event.get("avg(price)"));
		}
	}
}
