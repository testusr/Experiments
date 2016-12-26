package smeo.experiments.codegen.apt;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Set;

/**
 * https://deors.wordpress.com/2011/10/08/annotation-processors/
 */
@SupportedAnnotationTypes("smeo.experiments.codegen.apt.Complexity")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class ComplexityProcessor extends AbstractProcessor {

    public ComplexityProcessor() {
        super();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations,
                           RoundEnvironment roundEnv) {
        System.err.println("process was called");

        for (Element elem : roundEnv.getElementsAnnotatedWith(Complexity.class)) {
            Complexity complexity = elem.getAnnotation(Complexity.class);
            String message = "annotation found in " + elem.getSimpleName()
                    + " with complexity " + complexity.value();
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, message);
        }
        return true; // no further processing of this annotation type
    }
}