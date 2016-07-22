package smeo.experiments.test.utils;

import com.thoughtworks.xstream.XStream;
import junit.framework.TestCase;
import smeo.experiments.test.utils.model.TestClassA;

/**
 * Created by smeo on 21.07.16.
 */
public class RandomClassInstanceGeneratorTest extends TestCase {
    public void testSimple(){
        RandomClassInstanceGenerator<TestClassA> generator = new RandomClassInstanceGenerator<TestClassA>();
        TestClassA generatedInstance = generator.generateClassInstance(TestClassA.class);
        XStream xStream = new XStream();
        System.out.println(xStream.toXML(generatedInstance));
    }
}