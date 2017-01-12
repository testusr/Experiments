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
import java.util.*;

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

        List<VariableElement> fieldsToProcess = new ArrayList<VariableElement>();


        for (Element e : roundEnv.getElementsAnnotatedWith(Efficient.class)) {
            if (!processClass(filer, e)) {
                if (e.getKind() == ElementKind.FIELD) {

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
        }
        return true;
    }

    private boolean processClass(Filer filer, Element e) {
        if (e.getKind() == ElementKind.CLASS) {

            String fqClassName;
            String className;
            String packageName;
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

            List<VariableElement> fieldsToProcess = extractFields(enclosedElements);
            int i = 0;
            while (!fieldsToProcess.isEmpty()) {
                VariableElement currField = fieldsToProcess.get(i++ % fieldsToProcess.size());
                verifyDependentFieldsAreAlreadySerialized(currField, fieldsToProcess);
                //  if (!verifyDependentFieldsAreAlreadySerialized(currField, fieldsToProcess)) {
                appendWriteExternalCommand(writeExternalMethod, readExternalMethod, currField);
                fieldsToProcess.remove(currField);
//                }
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
            return true;
        }
        return false;
    }

    private String getFieldReflectingCurrentArraySize(Element arrayField) {
        final List<? extends Element> enclosedElements = arrayField.getEnclosedElements();
        for (AnnotationMirror currAnnotation : arrayField.getAnnotationMirrors()) {
            if (isAssignable(currAnnotation.getAnnotationType(), EfficientArray.class)) {
                final Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = currAnnotation.getElementValues();
                for (ExecutableElement currElement : elementValues.keySet()) {
                    if ("sizeField".equals(currElement.getSimpleName().toString())) {
                        final AnnotationValue annotationValue = elementValues.get(currElement);
                        return (String) annotationValue.getValue();
                    }
                }
            }
        }
        return null;
    }

    private void verifyDependentFieldsAreAlreadySerialized(VariableElement field, List<VariableElement> fieldsToProcess) {
        String arraySizeField = getFieldReflectingCurrentArraySize(field);
        if (arraySizeField != null) {
            if (containsFieldWithName(arraySizeField, fieldsToProcess)) {
                final String errorMessage = "annotated array '" + field.getSimpleName() + "' depends on value '"
                        + arraySizeField + "' which has to be externalized first. please reorder the attributes " +
                        "accordingly";
                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }
        }
    }

    private boolean containsFieldWithName(String fieldName, List<VariableElement> fieldList) {
        for (VariableElement currVariable : fieldList) {
            if (currVariable.getSimpleName().toString().equals(fieldName)) {
                return true;
            }
        }
        return false;
    }

    private List<VariableElement> extractFields(List<? extends Element> enclosedElements) {
        List<VariableElement> fields = new ArrayList<VariableElement>();
        for (Element e : enclosedElements) {
            if (e.getKind() == ElementKind.FIELD) {
                fields.add((VariableElement) e);
            }
        }
        return fields;
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
                    String sizeField = getFieldReflectingCurrentArraySize(currElement);
                    if (sizeField != null) {
                        writeArrayWithSizeField(writeExternalMethod, ((ArrayType) typeMirror).getComponentType(), kind1, sizeField, simpleName.toString());
                        readArrayWithSizeField(readExternalMethod, ((ArrayType) typeMirror).getComponentType(), kind1, sizeField, simpleName.toString());
                    } else {
                        writeArray(writeExternalMethod, ((ArrayType) typeMirror).getComponentType(), kind1, simpleName.toString());
                        readArray(readExternalMethod, ((ArrayType) typeMirror).getComponentType(), kind1, simpleName.toString());
                    }
                } else {
                    writeExternalMethod.addStatement(createWriteStatement(typeMirror, kind1, simpleName.toString()));
                    readExternalMethod.addStatement(createReadStatement(typeMirror, kind1, simpleName.toString()));
                }

            }
        }
    }

    private void readArrayWithSizeField(MethodSpec.Builder readExternalMsgBody, TypeMirror componentType, TypeKind kind1, String sizeFieldName, String arrayFieldName) {
        readExternalMsgBody.beginControlFlow("for (int i =0; i < " + sizeFieldName + "; i++)");
        readExternalMsgBody.addStatement(createReadStatement(componentType, kind1, (arrayFieldName + "[i]")));
        readExternalMsgBody.endControlFlow();
    }

    private void writeArrayWithSizeField(MethodSpec.Builder writeExternalMsgBody, TypeMirror componentType, TypeKind kind1, String sizeFieldName, String arrayFieldName) {
        writeExternalMsgBody.beginControlFlow("for (int i=0; i <  " + sizeFieldName + "; i++)");
        writeExternalMsgBody.addStatement(createWriteStatement(componentType, kind1, (arrayFieldName + "[i]")));
        writeExternalMsgBody.endControlFlow();
    }

    private void readArray(MethodSpec.Builder readExternalMsgBody, TypeMirror typeMirror, TypeKind kind1, String simpleName) {
        String fieldArrayLength = simpleName + "Length";

        readExternalMsgBody.addStatement("int " + fieldArrayLength + "=in.readInt();");
        readExternalMsgBody.beginControlFlow("for (int i =0; i < " + fieldArrayLength + "; i++)");
        readExternalMsgBody.addStatement(createReadStatement(typeMirror, kind1, (simpleName + "[i]")));
        readExternalMsgBody.endControlFlow();
    }

    private void writeArray(MethodSpec.Builder writeExternalMsgBody, TypeMirror typeMirror, TypeKind kind1, String arrayFieldName) {
        String fieldArrayLength = arrayFieldName + "Length";

        writeExternalMsgBody.addStatement("int " + fieldArrayLength + " = 0");
        writeExternalMsgBody.beginControlFlow("if (" + arrayFieldName + " != null)");
        writeExternalMsgBody.addStatement(fieldArrayLength + "= " + arrayFieldName + ".length");
        writeExternalMsgBody.endControlFlow();
        writeExternalMsgBody.addStatement("out.writeInt(" + fieldArrayLength + ")");
        writeExternalMsgBody.beginControlFlow("for (int i=0; i <  " + fieldArrayLength + "; i++)");
        writeExternalMsgBody.addStatement(createWriteStatement(typeMirror, kind1, (arrayFieldName + "[i]")));
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
