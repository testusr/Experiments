package smeo.experiments.codegen.apt;

import com.squareup.javapoet.*;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
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
 * <p>
 * // http://hauchee.blogspot.de/2015/12/compile-time-annotation-processing-getting-class-value.html
 * // http://www.programcreek.com/java-api-examples/index.php?api=javax.lang.model.type.TypeMirror
 * http://hannesdorfmann.com/annotation-processing/annotationprocessing101
 */
@SupportedAnnotationTypes("smeo.experiments.codegen.apt.Efficient")
public class EfficientAnnotationProcessor extends AbstractProcessor {
    boolean inError = false;
    List<Element> efficientElements = new ArrayList<Element>();
    Map<String, EfficientField> originalFieldNameToEfficientField = new HashMap<String, EfficientField>();
    int round = -1;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        inError = false;
        round++;
        final Set<? extends Element> elementsAnnotatedWithEfficient = roundEnv.getElementsAnnotatedWith(Efficient.class);
        efficientElements.addAll(elementsAnnotatedWithEfficient);
        System.err.println("EfficientAnnotationProcessor was called");

        final Map<String, String> options = processingEnv.getOptions();
        final Filer filer = processingEnv.getFiler();

        String fqClassName = null;
        String className = null;
        String packageName = null;
        Map<String, VariableElement> fields = new HashMap<String, VariableElement>();
        Map<String, ExecutableElement> methods = new HashMap<String, ExecutableElement>();

        List<VariableElement> fieldsToProcess = new ArrayList<VariableElement>();


        for (Element e : elementsAnnotatedWithEfficient) {
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

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    private boolean processClass(Filer filer, Element e) {
        if (e.getKind() == ElementKind.CLASS) {

            TypeElement classElement = (TypeElement) e;
            PackageElement packageElement = (PackageElement) classElement.getEnclosingElement();
            String className = classElement.getSimpleName().toString();
            String packageName = packageElement.getQualifiedName().toString();
            String fqClassName = classElement.getQualifiedName().toString();


            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.NOTE,
                    "processing annotated class: " + classElement.getQualifiedName(), e);

            final List<? extends Element> enclosedElements = e.getEnclosedElements();

            final ClassName className1 = ClassName.get(packageName, className);

            final MethodSpec.Builder staticWriteExternalMethod = MethodSpec.methodBuilder("writeExternal")
                    .addParameter(className1, "src", Modifier.FINAL)
                    .addParameter(ObjectOutput.class, "out", Modifier.FINAL)
                    .addException(IOException.class)
                    .addModifiers(Modifier.PUBLIC)
                    .addModifiers(Modifier.STATIC)
                    .returns(void.class);

            final MethodSpec.Builder staticReadExternalMethod = MethodSpec.methodBuilder("readExternal")
                    .addParameter(className1, "target", Modifier.FINAL)
                    .addParameter(ObjectInput.class, "in", Modifier.FINAL)
                    .addException(IOException.class)
                    .addException(ClassNotFoundException.class)
                    .addModifiers(Modifier.PUBLIC)
                    .addModifiers(Modifier.STATIC)
                    .returns(void.class);

            final MethodSpec.Builder writeExternalMethod = MethodSpec.methodBuilder("writeExternal")
                    .addParameter(ObjectOutput.class, "out", Modifier.FINAL)
                    .addException(IOException.class)
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .addStatement("writeExternal(this, out)")
                    .returns(void.class);


            final MethodSpec.Builder readExternalMethod = MethodSpec.methodBuilder("readExternal")
                    .addParameter(ObjectInput.class, "in", Modifier.FINAL)
                    .addException(IOException.class)
                    .addException(ClassNotFoundException.class)
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .addStatement("readExternal(this, in)")
                    .returns(void.class);


            StringBuilder writeExernalMessageBody = new StringBuilder();
            StringBuilder readExernalMessageBody = new StringBuilder();

            List<VariableElement> fieldsToProcess = extractFields(enclosedElements);
            List<VariableElement> fieldsProcessed = new ArrayList<VariableElement>();

            final Iterator<VariableElement> iterator = fieldsToProcess.iterator();
            for (VariableElement currField : fieldsToProcess) {
                verifyDependentFieldsAreAlreadySerialized(currField, fieldsProcessed);
                appendWriteExternalCommand(staticWriteExternalMethod, staticReadExternalMethod, currField);
                if (inError) {
                    break;
                }
                fieldsProcessed.add(currField);
            }


            if (!inError) {

                final String generatedClassName = toEfficientClassName(className);
                TypeSpec efficientVersion = TypeSpec.classBuilder(generatedClassName)
                        .addModifiers(Modifier.PUBLIC)
                        .superclass(TypeName.get(e.asType()))
                        .addSuperinterface(Externalizable.class)
                        .addMethod(staticWriteExternalMethod.build())
                        .addMethod(staticReadExternalMethod.build())
                        .addMethod(writeExternalMethod.build())
                        .addMethod(readExternalMethod.build())
                        .build();


                JavaFile javaFile = JavaFile.builder(packageName, efficientVersion)
                        //.indent("   ")
                        .build();
                try {
                    javaFile.writeTo(filer);

                    processingEnv.getMessager().printMessage(
                            Diagnostic.Kind.NOTE,
                            "... generated efficient class " + generatedClassName, e);

                } catch (IOException e1) {
                    e1.printStackTrace();
                    inError = true;
                }
                return !inError;
            } else {

                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        "... error occured while processing class '" + className + " / round '" + round + "''. No efficient version generated");
            }
        }

        return false;
    }

    private String toEfficientClassName(String className) {
        final int lastDotIndex = className.lastIndexOf('.');
        if (lastDotIndex > 0) {
            String simpleClassName = className.substring(lastDotIndex + 1, className.length());
            return className.replace(simpleClassName, "Efficient" + simpleClassName);
        }
        return "Efficient" + className;
    }

    private String toEfficientFieldName(String fieldName) {
        return "efficient" + fieldName;
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
            if (!containsFieldWithName(arraySizeField, fieldsToProcess)) {
                final String errorMessage = "annotated array '" + field.getSimpleName() + "' depends on value '"
                        + arraySizeField + "' which has to be externalized first. please reorder the attributes " +
                        "accordingly";
                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        errorMessage);
                inError = true;
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


    private void appendWriteExternalCommand(MethodSpec.Builder writeExternalMethod, MethodSpec.Builder readExternalMethod, Element currField) {
        final ElementKind kind = currField.getKind();

        if (ElementKind.FIELD.equals(kind)) {
            final TypeMirror fieldTypeMirror = currField.asType();


            final Class<? extends Element> aClass = currField.getClass();
            final TypeKind kind1 = fieldTypeMirror.getKind();
            final String className = fieldTypeMirror.toString();
            final String name = aClass.getName();
            final Field[] declaredFields = aClass.getDeclaredFields();
            final boolean primitive = aClass.isPrimitive();
            final String fieldName = currField.getSimpleName().toString();

            String efficientFieldReplacement = null;


            if (!currField.getModifiers().contains(Modifier.TRANSIENT)) {
                if (classTypeIsAnnotatatedAsEfficient(fieldTypeMirror)) {
                    writeExternalMethod.addStatement(createWriteEffectiveField(toEfficientClassName(fieldTypeMirror.toString()), fieldName));
                    readExternalMethod.addStatement(createReadEffectiveField(toEfficientClassName(fieldTypeMirror.toString()), fieldName));
                } else {
                    if (kind1.equals(TypeKind.ARRAY)) {
                        String sizeField = getFieldReflectingCurrentArraySize(currField);
                        if (sizeField != null) {
                            writeArrayWithSizeField(writeExternalMethod, ((ArrayType) fieldTypeMirror).getComponentType(), kind1, sizeField, fieldName);
                            readArrayWithSizeField(readExternalMethod, ((ArrayType) fieldTypeMirror).getComponentType(), kind1, sizeField, fieldName);
                        } else {
                            writeArray(writeExternalMethod, ((ArrayType) fieldTypeMirror).getComponentType(), kind1, fieldName);
                            readArray(readExternalMethod, ((ArrayType) fieldTypeMirror).getComponentType(), kind1, fieldName);
                        }
                    } else {
                        writeExternalMethod.addStatement(createWriteStatement(fieldTypeMirror, kind1, fieldName));
                        readExternalMethod.addStatement(createReadStatement(fieldTypeMirror, kind1, fieldName));
                    }
                }

            }
        }
    }

    private String createReadEffectiveField(String effectivClassName, String fieldName) {
        return effectivClassName + ".readExternal( target." + fieldName + ", in)";
    }

    private String createWriteEffectiveField(String effectivClassName, String fieldName) {
        return effectivClassName + ".writeExternal( src." + fieldName + ", out)";

    }

    private void readArrayWithSizeField(MethodSpec.Builder readExternalMsgBody, TypeMirror componentType, TypeKind kind1, String sizeFieldName, String arrayFieldName) {
        String fqSizeFieldName = "target." + sizeFieldName;
        readExternalMsgBody.beginControlFlow("for (int i =0; i < " + fqSizeFieldName + "; i++)");
        readExternalMsgBody.addStatement(createReadStatement(componentType, kind1, (arrayFieldName + "[i]")));
        readExternalMsgBody.endControlFlow();
    }

    private void writeArrayWithSizeField(MethodSpec.Builder writeExternalMsgBody, TypeMirror componentType, TypeKind kind1, String sizeFieldName, String arrayFieldName) {
        String fqSizeFieldName = "src." + sizeFieldName;
        String fqArrayFieldName = arrayFieldName;
        writeExternalMsgBody.beginControlFlow("for (int i=0; i <  " + fqSizeFieldName + "; i++)");
        writeExternalMsgBody.addStatement(createWriteStatement(componentType, kind1, (fqArrayFieldName + "[i]")));
        writeExternalMsgBody.endControlFlow();
    }

    private void readArray(MethodSpec.Builder readExternalMsgBody, TypeMirror typeMirror, TypeKind kind1, String arrayFieldName) {
        String fieldArrayLength = arrayFieldName + "Length";

        readExternalMsgBody.addStatement("int " + fieldArrayLength + "=in.readInt();");
        readExternalMsgBody.beginControlFlow("for (int i =0; i < " + fieldArrayLength + "; i++)");
        readExternalMsgBody.addStatement(createReadStatement(typeMirror, kind1, (arrayFieldName + "[i]")));
        readExternalMsgBody.endControlFlow();
    }

    private void writeArray(MethodSpec.Builder writeExternalMsgBody, TypeMirror typeMirror, TypeKind kind1, String arrayFieldName) {
        String fieldArrayLength = arrayFieldName + "Length";

        String fqArrayFieldName = "src." + arrayFieldName;
        writeExternalMsgBody.addStatement("int " + fieldArrayLength + " = 0");
        writeExternalMsgBody.beginControlFlow("if (" + fqArrayFieldName + " != null)");
        writeExternalMsgBody.addStatement(fieldArrayLength + "= " + fqArrayFieldName + ".length");
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

    private StringBuilder appendWriteStatement(StringBuilder statement, TypeMirror currType, TypeKind kind1, String fieldName) {
        if (TypeKind.ERROR.equals(kind1)) {
            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR,
                    "error trying to translate field for write statement '" + fieldName + "'");
            return statement;
        }
        String fqFieldName = "src." + fieldName;
        if (TypeKind.LONG.equals(kind1) || isAssignable(currType, Long.class)) {
            statement.append("out.writeLong(" + fqFieldName + ")");
        } else if (TypeKind.DOUBLE.equals(kind1) || isAssignable(currType, Double.class)) {
            statement.append("out.writeDouble(" + fqFieldName + ")");
        } else if (TypeKind.FLOAT.equals(kind1) || isAssignable(currType, Float.class)) {
            statement.append("out.writeFloat(" + fqFieldName + ")");
        } else if (TypeKind.BOOLEAN.equals(kind1) || isAssignable(currType, Boolean.class)) {
            statement.append("out.writeBoolean(" + fqFieldName + ")");
        } else if (TypeKind.INT.equals(kind1) || isAssignable(currType, Integer.class)) {
            statement.append("out.writeInt(" + fqFieldName + ")");
        } else if (TypeKind.SHORT.equals(kind1) || isAssignable(currType, Short.class)) {
            statement.append("out.writeShort(" + fqFieldName + ")");
        } else if (TypeKind.CHAR.equals(kind1) || isAssignable(currType, Character.class)) {
            statement.append("out.writeChar(" + fqFieldName + ")");
        } else if (implementsInterface(currType, Externalizable.class)) {
            statement.append(fqFieldName + ".writeExternal(out)");
        } else if (implementsInterface(currType, Serializable.class)) {
            statement.append("out.writeObject(" + fqFieldName + ")");
        } else {
            throw new IllegalArgumentException("cannot determine how to externalize object '" + fqFieldName + ": " + currType.toString() + "'");
        }
        return statement;
    }

    private StringBuilder appendReadStatement(StringBuilder statement, TypeMirror currType, TypeKind kind1, String fieldName) {
        if (TypeKind.ERROR.equals(kind1)) {
            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR,
                    "error trying to translate field for read statement '" + fieldName + "'");
            return statement;
        }

        String fqFieldName = "target." + fieldName;

        if (TypeKind.LONG.equals(kind1) || isAssignable(currType, Long.class)) {
            statement.append(fqFieldName + " = in.readLong()");
        } else if (TypeKind.DOUBLE.equals(kind1) || isAssignable(currType, Double.class)) {
            statement.append(fqFieldName + " = in.readDouble()");
        } else if (TypeKind.FLOAT.equals(kind1) || isAssignable(currType, Float.class)) {
            statement.append(fqFieldName + " = in.readFloat()");
        } else if (TypeKind.BOOLEAN.equals(kind1) || isAssignable(currType, Boolean.class)) {
            statement.append(fqFieldName + " = in.readBoolean()");
        } else if (TypeKind.INT.equals(kind1) || isAssignable(currType, Integer.class)) {
            statement.append(fqFieldName + " = in.readInt()");
        } else if (TypeKind.SHORT.equals(kind1) || isAssignable(currType, Short.class)) {
            statement.append(fqFieldName + " = in.readShort()");
        } else if (TypeKind.CHAR.equals(kind1) || isAssignable(currType, Character.class)) {
            statement.append(fqFieldName + " = in.readChar()");
        } else if (implementsInterface(currType, Externalizable.class)) {
            statement.append(fqFieldName + ".readExternal(in)");
        } else if (implementsInterface(currType, Serializable.class)) {
            statement.append(fqFieldName + " = (" + currType + ") in.readObject()");
        } else {
            throw new IllegalArgumentException("cannot determine how to externalize object '" + fqFieldName + ":+" + currType.toString() + "'");
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

    private boolean classTypeIsAnnotatatedAsEfficient(TypeMirror type) {
        for (Element currElement : efficientElements) {
            if (currElement.asType().equals(type)) {
                return true;
            }
        }
        return false;
    }


    private class EfficientField {
        private String unefficientFieldName;
        private TypeMirror unefficientFieldType;

        public EfficientField(String unefficientFieldName, TypeMirror unefficientFieldType) {
            this.unefficientFieldName = unefficientFieldName;
            this.unefficientFieldType = unefficientFieldType;
        }
    }
}
