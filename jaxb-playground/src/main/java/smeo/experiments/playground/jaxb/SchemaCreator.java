package smeo.experiments.playground.jaxb;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;

/**
 * Created by truehl on 08.06.16.
 */
public class SchemaCreator {
	public static void main(String[] args) {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(Customer.class);
			SchemaOutputResolver sor = new MySchemaOutputResolver();
			jaxbContext.generateSchema(sor);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static class MySchemaOutputResolver extends SchemaOutputResolver {

		public Result createOutput(String namespaceURI, String suggestedFileName) throws IOException {
			File file = new File(suggestedFileName);
			StreamResult result = new StreamResult(file);
			result.setSystemId(file.toURI().toURL().toString());
			System.out.println(file.getName());
			return result;
		}

	}
}
