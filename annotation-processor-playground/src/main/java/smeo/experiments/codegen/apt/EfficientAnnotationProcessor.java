package smeo.experiments.codegen.apt;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.ArrayType;
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

        final Map<String, String> options = processingEnv.getOptions();
        final Filer filer = processingEnv.getFiler();

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


                final MethodSpec.Builder writeExternalMethod = MethodSpec.methodBuilder("writeExternal")
                        .addParameter(ObjectOutput.class, "out", Modifier.FINAL)
                        .addException(IOException.class)
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(void.class);

                final MethodSpec.Builder readExternalMethod = MethodSpec.methodBuilder("readExternal")
                        .addParameter(ObjectInput.class, "in", Modifier.FINAL)
                        .addException(IOException.class)
                        .addException(ClassNotFoundException.class)
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(void.class);


                StringBuilder writeExernalMessageBody = new StringBuilder();
                StringBuilder readExernalMessageBody = new StringBuilder();
                for (Element currElement : enclosedElements) {
                    appendWriteExternalCommand(writeExternalMethod, readExternalMethod, currElement);
                }

                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.NOTE,
                        "annotated class: " + classElement.getQualifiedName(), e);

                fqClassName = classElement.getQualifiedName().toString();
                className = classElement.getSimpleName().toString();
                packageName = packageElement.getQualifiedName().toString();


                TypeSpec efficientVersion = TypeSpec.classBuilder("Efficient" + className)
                        .addModifiers(Modifier.PUBLIC)
                        .superclass(TypeName.get(e.asType()))
                        .addSuperinterface(Externalizable.class)
                        .addMethod(writeExternalMethod.build())
                        .addMethod(readExternalMethod.build())
                        .build();


                JavaFile javaFile = JavaFile.builder(packageName, efficientVersion)
                        //.indent("   ")
                        .build();
                try {
                    javaFile.writeTo(filer);
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
    private void appendWriteExternalCommand(MethodSpec.Builder writeExternalMethod, MethodSpec.Builder readExternalMethod, Element currElement) {
        final ElementKind kind = currElement.getKind();

        if (ElementKind.FIELD.equals(kind)) {
            final TypeMirror typeMirror = currElement.asType();


            final Class<? extends Element> aClass = currElement.getClass();
            final TypeKind kind1 = typeMirror.getKind();
            final String className = typeMirror.toString();
            final String name = aClass.getName();
            final Field[] declaredFields = aClass.getDeclaredFields();
            final boolean primitive = aClass.isPrimitive();
            final Name simpleName = currElement.getSimpleName();

            if (!currElement.getModifiers().contains(Modifier.TRANSIENT)) {
                if (kind1.equals(TypeKind.ARRAY)) {
                    writeArray(writeExternalMethod, ((ArrayType) typeMirror).getComponentType(), kind1, simpleName.toString());
                    readArray(readExternalMethod, ((ArrayType) typeMirror).getComponentType(), kind1, simpleName.toString());
                } else {
                    writeExternalMethod.addStatement(createWriteStatement(typeMirror, kind1, simpleName.toString()));
                    readExternalMethod.addStatement(createReadStatement(typeMirror, kind1, simpleName.toString()));
                }

            }
        }
    }

    private void readArray(MethodSpec.Builder readExternalMsgBody, TypeMirror typeMirror, TypeKind kind1, String simpleName) {
        String fieldArrayLength = simpleName + "Length";
        String indexName = "i";

        readExternalMsgBody.addStatement("int " + fieldArrayLength + "=in.readInt();");
        readExternalMsgBody.beginControlFlow("for (int i=0; i < " + fieldArrayLength + "; i++)");
        readExternalMsgBody.addStatement(createReadStatement(typeMirror, kind1, (simpleName + "[" + indexName + "]")));
        readExternalMsgBody.endControlFlow();
    }

    private void writeArray(MethodSpec.Builder writeExternalMsgBody, TypeMirror typeMirror, TypeKind kind1, String simpleName) {
        String fieldArrayLength = simpleName + "Length";
        String indexName = "i";

        writeExternalMsgBody.addStatement("int " + fieldArrayLength + " = 0");
        writeExternalMsgBody.beginControlFlow("if (" + simpleName + " != null)");
        writeExternalMsgBody.addStatement(fieldArrayLength + "= " + simpleName + ".length");
        writeExternalMsgBody.endControlFlow();
        writeExternalMsgBody.addStatement("out.writeInt(" + fieldArrayLength + ")");
        writeExternalMsgBody.beginControlFlow("for (int i=0; " + indexName + " <  " + fieldArrayLength + "; " + indexName + "++)");
        writeExternalMsgBody.addStatement(createWriteStatement(typeMirror, kind1, (simpleName + "[" + indexName + "]")));
        writeExternalMsgBody.endControlFlow();
    }


    private String createWriteStatement(TypeMirror currType, TypeKind kind1, String simpleName) {
        return appendWriteStatement(new StringBuilder(), currType, kind1, simpleName).toString();
    }

    private String createReadStatement(TypeMirror currType, TypeKind kind1, String simpleName) {
        return appendReadStatement(new StringBuilder(), currType, kind1, simpleName).toString();
    }

    private StringBuilder appendWriteStatement(StringBuilder statement, TypeMirror currType, TypeKind kind1, String simpleName) {
        if (TypeKind.LONG.equals(kind1) || isAssignable(currType, Long.class)) {
            statement.append("out.writeLong(" + simpleName + ")");
        } else if (TypeKind.DOUBLE.equals(kind1) || isAssignable(currType, Double.class)) {
            statement.append("out.writeDouble(" + simpleName + ")");
        } else if (TypeKind.FLOAT.equals(kind1) || isAssignable(currType, Float.class)) {
            statement.append("out.writeFloat(" + simpleName + ")");
        } else if (TypeKind.BOOLEAN.equals(kind1) || isAssignable(currType, Boolean.class)) {
            statement.append("out.writeBoolean(" + simpleName + ")");
        } else if (TypeKind.INT.equals(kind1) || isAssignable(currType, Integer.class)) {
            statement.append("out.writeInt(" + simpleName + ")");
        } else if (TypeKind.SHORT.equals(kind1) || isAssignable(currType, Short.class)) {
            statement.append("out.writeShort(" + simpleName + ")");
        } else if (TypeKind.CHAR.equals(kind1) || isAssignable(currType, Character.class)) {
            statement.append("out.writeChar(" + simpleName + ")");
        } else if (implementsInterface(currType, Externalizable.class)) {
            statement.append(simpleName + ".writeExternal(out)");
        } else if (implementsInterface(currType, Serializable.class)) {
            statement.append("out.writeObject(" + simpleName + ")");
        } else {
            throw new IllegalArgumentException("cannot determine how to externalize object '" + simpleName + ":+" + currType.toString() + "'");
        }
        return statement;
    }

    private StringBuilder appendReadStatement(StringBuilder statement, TypeMirror currType, TypeKind kind1, String simpleName) {
        if (TypeKind.LONG.equals(kind1) || isAssignable(currType, Long.class)) {
            statement.append(simpleName + " = in.readLong()");
        } else if (TypeKind.DOUBLE.equals(kind1) || isAssignable(currType, Double.class)) {
            statement.append(simpleName + " = in.readDouble()");
        } else if (TypeKind.FLOAT.equals(kind1) || isAssignable(currType, Float.class)) {
            statement.append(simpleName + " = in.readFloat()");
        } else if (TypeKind.BOOLEAN.equals(kind1) || isAssignable(currType, Boolean.class)) {
            statement.append(simpleName + " = in.readBoolean()");
        } else if (TypeKind.INT.equals(kind1) || isAssignable(currType, Integer.class)) {
            statement.append(simpleName + " = in.readInt()");
        } else if (TypeKind.SHORT.equals(kind1) || isAssignable(currType, Short.class)) {
            statement.append(simpleName + " = in.readShort()");
        } else if (TypeKind.CHAR.equals(kind1) || isAssignable(currType, Character.class)) {
            statement.append(simpleName + " = in.readChar()");
        } else if (implementsInterface(currType, Externalizable.class)) {
            statement.append(simpleName + ".readExternal(in)");
        } else if (implementsInterface(currType, Serializable.class)) {
            statement.append(simpleName + " = (" + currType + ") in.readObject()");
        } else {
            throw new IllegalArgumentException("cannot determine how to externalize object '" + simpleName + ":+" + currType.toString() + "'");
        }
        return statement;
    }

    private boolean isAssignable(TypeMirror typeMirror, Class<?> interfc) {
        final TypeMirror externalizableTypeMirror = processingEnv.getElementUtils()
                .getTypeElement(interfc.getName()).asType();

        return processingEnv.getTypeUtils().isAssignable(typeMirror, externalizableTypeMirror);
    }

    private boolean implementsInterface(TypeMirror typeMirror, Class<?> interfc) {
        final TypeMirror externalizableTypeMirror = processingEnv.getElementUtils()
                .getTypeElement(interfc.getName()).asType();

        return processingEnv.getTypeUtils().isAssignable(typeMirror, externalizableTypeMirror);
    }


}
