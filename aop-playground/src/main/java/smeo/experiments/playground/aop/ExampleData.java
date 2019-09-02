package smeo.experiments.playground.aop;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by smeo on 28.04.16.
 */
public class ExampleData {
    private String privateAttribute = null;
    private List<ExampleReference> references;

    public void ExampleData(){
        references = new ArrayList<ExampleReference>();
    }
    public void updateAttributes(){
        privateAttribute = "newValue";
        privateAttribute = "newValue#2";
        privateAttribute = "newValue#3";
    }

    public void updateReferences(){
        final ExampleReference exampleReference1 = new ExampleReference(1);
        final ExampleReference exampleReference2 = new ExampleReference(2);
        final ExampleReference exampleReference3 = new ExampleReference(3);
        final ExampleReference exampleReference4 = new ExampleReference(4);

        references.add(exampleReference1);
        references.add(exampleReference2);

        references.set(2, exampleReference4);
        references.remove(exampleReference4);
        references.remove(2);
    }

}
