package smeo.experiments.codegen.apt;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by smeo on 26.12.16.
 */
@SupportedAnnotationTypes("smeo.experiments.codegen.apt.Efficient")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class EfficientAnnotationProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        System.err.println("process2 was called");
        String fqClassName = null;
        String className = null;
        String packageName = null;
        Map<String, VariableElement> fields = new HashMap<String, VariableElement>();
        Map<String, ExecutableElement> methods = new HashMap<String, ExecutableElement>();

        for (Element e : roundEnv.getElementsAnnotatedWith(Efficient.class)) {

            if (e.getKind() == ElementKind.CLASS) {

                TypeElement classElement = (TypeElement) e;
                PackageElement packageElement = (PackageElement) classElement.getEnclosingElement();

                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.NOTE,
                        "annotated class: " + classElement.getQualifiedName(), e);

                fqClassName = classElement.getQualifiedName().toString();
                className = classElement.getSimpleName().toString();
                packageName = packageElement.getQualifiedName().toString();

            } else if (e.getKind() == ElementKind.FIELD) {

                VariableElement varElement = (VariableElement) e;

                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.NOTE,
                        "annotated field: " + varElement.getSimpleName(), e);

                fields.put(varElement.getSimpleName().toString(), varElement);

            } else if (e.getKind() == ElementKind.METHOD) {

                ExecutableElement exeElement = (ExecutableElement) e;

                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.NOTE,
                        "annotated method: " + exeElement.getSimpleName(), e);

                methods.put(exeElement.getSimpleName().toString(), exeElement);
            }
        }
        return true;
    }
}
