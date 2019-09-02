package smeo.experiments.playground.jaxb;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Simple example to marshal and unmarshal an object from to xml
 */
public class JAXBExample {
	public static void main(String[] args) {

		Customer customer = new Customer();
		customer.setId(100);
		customer.setName("mkyong");
		customer.setAge(29);

		String xmlString = null;
		// marshal
		try {

			// File file = new File("\file.xml");
			JAXBContext jaxbContext = JAXBContext.newInstance(Customer.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

			// output pretty printed
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			// jaxbMarshaller.marshal(customer, file);
			jaxbMarshaller.marshal(customer, System.out);

			StringWriter stringWriter = new StringWriter();
			jaxbMarshaller.marshal(customer, stringWriter);
			xmlString = stringWriter.toString();

		} catch (JAXBException e) {
			e.printStackTrace();
		}

		// unmarshal
		try {

			// File file = new File("\file.xml");
			JAXBContext jaxbContext = JAXBContext.newInstance(Customer.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

			StringReader stringReader = new StringReader(xmlString);
			Customer unmarschalledCustomer = (Customer) jaxbUnmarshaller.unmarshal(stringReader);

		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}
}
