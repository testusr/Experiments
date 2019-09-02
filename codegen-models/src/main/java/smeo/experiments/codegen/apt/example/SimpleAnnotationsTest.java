package smeo.experiments.codegen.apt.example;

import smeo.experiments.codegen.apt.Complexity;

/**
 * Created by smeo on 26.12.16.
 */
@Complexity(Complexity.ComplexityLevel.VERY_SIMPLE)
public class SimpleAnnotationsTest {

    public SimpleAnnotationsTest() {
        super();
    }

    @Complexity() // this annotation type applies also to methods
    // the default value 'ComplexityLevel.MEDIUM' is assumed
    public void theMethod() {
        System.out.println("consoleut");
    }
}