package smeo.experiments.codegen.apt;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.io.*;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Naiv Example implementation to generate efficient
 */
@SupportedAnnotationTypes("smeo.experiments.codegen.apt.Efficient")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class EfficientAnnotationProcessor extends AbstractProcessor {


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        System.err.println("EfficientAnnotationProcessor was called");

        File sourcePath = new File("src");
        String fqClassName = null;
        String className = null;
        String packageName = null;
        Map<String, VariableElement> fields = new HashMap<String, VariableElement>();
        Map<String, ExecutableElement> methods = new HashMap<String, ExecutableElement>();

        for (Element e : roundEnv.getElementsAnnotatedWith(Efficient.class)) {

            if (e.getKind() == ElementKind.CLASS) {

                TypeElement classElement = (TypeElement) e;
                PackageElement packageElement = (PackageElement) classElement.getEnclosingElement();

                final List<? extends Element> enclosedElements = e.getEnclosedElements();

                StringBuilder writeExernalMessageBody = new StringBuilder();
                StringBuilder readExernalMessageBody = new StringBuilder();
                for (Element currElement : enclosedElements) {
                    appendWriteExternalCommand(writeExernalMessageBody, readExernalMessageBody, currElement);
                }

                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.NOTE,
                        "annotated class: " + classElement.getQualifiedName(), e);

                fqClassName = classElement.getQualifiedName().toString();
                className = classElement.getSimpleName().toString();
                packageName = packageElement.getQualifiedName().toString();

                final MethodSpec writeExternal = MethodSpec.methodBuilder("writeExternal")
                        .addParameter(ObjectOutput.class, "out", Modifier.FINAL)
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addStatement(writeExernalMessageBody.toString())
                        .returns(void.class)
                        .build();

                final MethodSpec readExternal = MethodSpec.methodBuilder("readExternal")
                        .addParameter(ObjectInput.class, "in", Modifier.FINAL)
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addStatement(readExernalMessageBody.toString())
                        .returns(void.class)
                        .build();


                TypeSpec efficientVersion = TypeSpec.classBuilder("GenEfficient" + className)
                        .addModifiers(Modifier.PUBLIC)
                        .superclass(TypeName.get(e.asType()))
                        .addSuperinterface(Externalizable.class)
                        .addMethod(writeExternal)
                        .addMethod(readExternal)
                        .build();


                JavaFile javaFile = JavaFile.builder(packageName, efficientVersion).build();
                try {
                    javaFile.writeTo(sourcePath);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }


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

    // http://hauchee.blogspot.de/2015/12/compile-time-annotation-processing-getting-class-value.html
    // http://www.programcreek.com/java-api-examples/index.php?api=javax.lang.model.type.TypeMirror
    private void appendWriteExternalCommand(StringBuilder writeExternalMsgBody, StringBuilder readExternalMsgBody, Element currElement) {
        final ElementKind kind = currElement.getKind();

        if (ElementKind.FIELD.equals(kind)) {
            final TypeMirror typeMirror = currElement.asType();


            final Class<? extends Element> aClass = currElement.getClass();
            final TypeKind kind1 = typeMirror.getKind();
            final String s = typeMirror.toString();
            final String name = aClass.getName();
            final Field[] declaredFields = aClass.getDeclaredFields();
            final boolean primitive = aClass.isPrimitive();
            final Name simpleName = currElement.getSimpleName();

            if (TypeKind.LONG.equals(kind1) || s.equals(Long.class.getName())) {
                writeExternalMsgBody.append("out.writeLong(" + simpleName + ");\n");
                readExternalMsgBody.append(simpleName + " = in.readLong();\n");
            } else if (TypeKind.DOUBLE.equals(kind1) || s.equals(Double.class.getName())) {
                writeExternalMsgBody.append("out.writeDouble(" + simpleName + ");\n");
                readExternalMsgBody.append(simpleName + " = in.readDouble();\n");
            } else if (TypeKind.FLOAT.equals(kind1) || s.equals(Float.class.getName())) {
                writeExternalMsgBody.append("out.writeFloat(" + simpleName + ");\n");
                readExternalMsgBody.append(simpleName + " = in.readFloat();\n");
            } else if (TypeKind.BOOLEAN.equals(kind1) || s.equals(Boolean.class.getName())) {
                writeExternalMsgBody.append("out.writeBoolean(" + simpleName + ");\n");
                readExternalMsgBody.append(simpleName + " = in.readBoolean();\n");
            } else if (TypeKind.INT.equals(kind1) || s.equals(Integer.class.getName())) {
                writeExternalMsgBody.append("out.writeInt(" + simpleName + ");\n");
                readExternalMsgBody.append(simpleName + " = in.readInt();\n");
            } else if (TypeKind.SHORT.equals(kind1) || s.equals(Short.class.getName())) {
                writeExternalMsgBody.append("out.writeShort(" + simpleName + ");\n");
                readExternalMsgBody.append(simpleName + " = in.readShort();\n");
            } else if (TypeKind.CHAR.equals(kind1) || s.equals(Character.class.getName())) {
                writeExternalMsgBody.append("out.writeChar(" + simpleName + ");\n");
                readExternalMsgBody.append(simpleName + " = in.readChar();\n");
            } else if (implementesInterface(typeMirror, Externalizable.class)) {
                writeExternalMsgBody.append(simpleName + ".writeExternal(out);\n");
                readExternalMsgBody.append(simpleName + ".readExternal(in);\n");
            } else if (implementesInterface(typeMirror, Serializable.class)) {
                writeExternalMsgBody.append("out.writeObject(" + simpleName + ");\n");
                readExternalMsgBody.append(simpleName + " = in.readObject();\n");
            } else {
                throw new IllegalArgumentException("cannot determine how to externalize object '" + simpleName + ":+" + typeMirror.toString() + "'");
            }
        }
    }

    private boolean implementesInterface(TypeMirror typeMirror, Class<?> interfc) {
        final TypeMirror externalizableTypeMirror = processingEnv.getElementUtils()
                .getTypeElement(interfc.getName()).asType();

        return processingEnv.getTypeUtils().isAssignable(typeMirror, externalizableTypeMirror);
    }


}
