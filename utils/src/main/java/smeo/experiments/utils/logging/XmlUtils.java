package smeo.experiments.utils.logging;

import com.thoughtworks.xstream.XStream;

/**
 * Created by truehl on 11.05.16.
 */
public class XmlUtils {
	public static String objectToXml(final Object object) {
		XStream xstream = new XStream();
		return xstream.toXML(object);
	}

	public static Object xmlToObject(final String xmlString) {
		XStream xstream = new XStream();
		return xstream.fromXML(xmlString);
	}
}
