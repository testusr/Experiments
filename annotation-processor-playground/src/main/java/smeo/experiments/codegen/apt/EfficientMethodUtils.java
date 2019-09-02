package smeo.experiments.codegen.apt;

import com.squareup.javapoet.MethodSpec;
import org.apache.commons.lang3.Validate;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.io.Externalizable;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by smeo on 31.01.17.
 */
public class EfficientMethodUtils {
    public static final String SRC = "src";
    public static final String TARGET = "target";
    public static final String IN = "in";
    public static final String OUT = "out";
    public static final String EFFICIENT_PACKAGE = "smeo.experiments.codegen.apt";
    private final ProcessingEnvironment processingEnv;
    private final RoundEnvironment roundEnv;
    private boolean inError;

    public EfficientMethodUtils(ProcessingEnvironment processingEnv, RoundEnvironment roundEnv) {
        this.processingEnv = processingEnv;
        this.roundEnv = roundEnv;
    }

    public String getFieldReflectingCurrentArraySize(Element arrayField) {
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

    private boolean isAssignable(TypeMirror typeMirror, Class<?> interfce) {
        final TypeMirror interfaceTypeMirror = processingEnv.getElementUtils()
                .getTypeElement(interfce.getName()).asType();

        return processingEnv.getTypeUtils().isAssignable(typeMirror, interfaceTypeMirror);
    }

    public boolean appendReadStatement(MethodSpec.Builder readExternalMethod, TypeMirror fieldTypeMirror, String fieldName) {
        TypeKind fieldTypeKind = fieldTypeMirror.getKind();
        if (TypeKind.ERROR.equals(fieldTypeKind)) {
            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR,
                    "error trying to translate field for read statement '" + fieldName + "'");
            return false;
        }

        String fqFieldName = TARGET + "." + fieldName;

        if (classTypeIsAnnotatatedAsEfficient(fieldTypeMirror)) {
            readExternalMethod.addStatement(createReadEffectiveField(toEfficientClassName(fieldTypeMirror.toString()), fqFieldName));
        } else if (TypeKind.LONG.equals(fieldTypeKind) || isAssignable(fieldTypeMirror, Long.class)) {
            readExternalMethod.addStatement(fqFieldName + " = " + IN + ".readLong()");
        } else if (TypeKind.DOUBLE.equals(fieldTypeKind) || isAssignable(fieldTypeMirror, Double.class)) {
            readExternalMethod.addStatement(fqFieldName + " = " + IN + ".readDouble()");
        } else if (TypeKind.FLOAT.equals(fieldTypeKind) || isAssignable(fieldTypeMirror, Float.class)) {
            readExternalMethod.addStatement(fqFieldName + " = " + IN + ".readFloat()");
        } else if (TypeKind.BOOLEAN.equals(fieldTypeKind) || isAssignable(fieldTypeMirror, Boolean.class)) {
            readExternalMethod.addStatement(fqFieldName + " = " + IN + ".readBoolean()");
        } else if (TypeKind.INT.equals(fieldTypeKind) || isAssignable(fieldTypeMirror, Integer.class)) {
            readExternalMethod.addStatement(fqFieldName + " = " + IN + ".readInt()");
        } else if (TypeKind.SHORT.equals(fieldTypeKind) || isAssignable(fieldTypeMirror, Short.class)) {
            readExternalMethod.addStatement(fqFieldName + " = " + IN + ".readShort()");
        } else if (TypeKind.CHAR.equals(fieldTypeKind) || isAssignable(fieldTypeMirror, Character.class)) {
            readExternalMethod.addStatement(fqFieldName + " = " + IN + ".readChar()");
        } else if (implementsInterface(fieldTypeMirror, Externalizable.class)) {
            readExternalMethod.addStatement(fqFieldName + ".readExternal(" + IN + ")");
        } else if (implementsInterface(fieldTypeMirror, Serializable.class)) {
            readExternalMethod.addStatement(fqFieldName + " = (" + fieldTypeMirror + ") " + IN + ".readObject()");
        } else {
            inError = true;
            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR,
                    "cannot determine how to externalize field '" + fieldName + "' of type '" + fieldTypeMirror.toString() + "'" +
                            " either mark class as @GenEfficient or make it implement Serializable");
            return false;
        }
        return true;
    }

    public boolean appendWriteStatement(MethodSpec.Builder writeExternalMethod, TypeMirror fieldTypeMirror, String fieldName) {
        TypeKind fieldTypeKind = fieldTypeMirror.getKind();
        if (TypeKind.ERROR.equals(fieldTypeKind)) {
            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR,
                    "error trying to translate field for write statement '" + fieldName + "'");
            return false;
        }
        String fqFieldName = SRC + "." + fieldName;
        if (classTypeIsAnnotatatedAsEfficient(fieldTypeMirror)) {
            writeExternalMethod.addStatement(createWriteEffectiveField(toEfficientClassName(fieldTypeMirror.toString()), fqFieldName));
        } else if (TypeKind.LONG.equals(fieldTypeKind) || isAssignable(fieldTypeMirror, Long.class)) {
            writeExternalMethod.addStatement(OUT + ".writeLong(" + fqFieldName + ")");
        } else if (TypeKind.DOUBLE.equals(fieldTypeKind) || isAssignable(fieldTypeMirror, Double.class)) {
            writeExternalMethod.addStatement(OUT + ".writeDouble(" + fqFieldName + ")");
        } else if (TypeKind.FLOAT.equals(fieldTypeKind) || isAssignable(fieldTypeMirror, Float.class)) {
            writeExternalMethod.addStatement(OUT + ".writeFloat(" + fqFieldName + ")");
        } else if (TypeKind.BOOLEAN.equals(fieldTypeKind) || isAssignable(fieldTypeMirror, Boolean.class)) {
            writeExternalMethod.addStatement(OUT + ".writeBoolean(" + fqFieldName + ")");
        } else if (TypeKind.INT.equals(fieldTypeKind) || isAssignable(fieldTypeMirror, Integer.class)) {
            writeExternalMethod.addStatement(OUT + ".writeInt(" + fqFieldName + ")");
        } else if (TypeKind.SHORT.equals(fieldTypeKind) || isAssignable(fieldTypeMirror, Short.class)) {
            writeExternalMethod.addStatement(OUT + ".writeShort(" + fqFieldName + ")");
        } else if (TypeKind.CHAR.equals(fieldTypeKind) || isAssignable(fieldTypeMirror, Character.class)) {
            writeExternalMethod.addStatement(OUT + ".writeChar(" + fqFieldName + ")");
        } else if (implementsInterface(fieldTypeMirror, Externalizable.class)) {
            writeExternalMethod.addStatement(fqFieldName + ".writeExternal(" + OUT + ")");
        } else if (implementsInterface(fieldTypeMirror, Serializable.class)) {
            writeExternalMethod.addStatement(OUT + ".writeObject(" + fqFieldName + ")");
        } else {
            inError = true;
            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR,
                    "cannot determine how to externalize field '" + fieldName + "' of type '" + fieldTypeMirror.toString() + "'" +
                            " either mark class as @GenEfficient or make it implement Serializable");
            return false;
        }
        return true;
    }

    private boolean implementsInterface(TypeMirror typeMirror, Class<?> interfc) {
        final TypeMirror externalizableTypeMirror = processingEnv.getElementUtils()
                .getTypeElement(interfc.getName()).asType();

        return processingEnv.getTypeUtils().isAssignable(typeMirror, externalizableTypeMirror);
    }


    public void appendWriteArraysWithSameSizeField(MethodSpec.Builder writeExternalMsgBody, String sizeFieldName, List<VariableElement> arrayFields) {
        String fqSizeFieldName = SRC + "." + sizeFieldName;

        writeExternalMsgBody.beginControlFlow("for (" + "int i=0; i <  " + fqSizeFieldName + "; i++)");
        for (VariableElement currField : arrayFields) {
            String simpleArrayFieldName = currField.getSimpleName().toString();
            appendWriteStatement(writeExternalMsgBody, currField.asType(), simpleArrayFieldName + "[i]");
        }

        writeExternalMsgBody.endControlFlow();
    }

    public void appendWriteArrayFields(MethodSpec.Builder writeExternalMsgBody, List<VariableElement> arrayFields) {
        for (VariableElement currArray : arrayFields) {
            appendWriteArray(writeExternalMsgBody, currArray);
        }
    }

    private void appendWriteArray(MethodSpec.Builder writeExternalMsgBody, VariableElement arrayField) {
        Validate.isTrue(arrayField.asType().getKind() == TypeKind.ARRAY);
        final String simpleArrayFieldName = arrayField.getSimpleName().toString();
        String fqSizeFieldName = SRC + "." + simpleArrayFieldName + ".length";
        writeExternalMsgBody.beginControlFlow("for (" + "int i=0; i <  " + fqSizeFieldName + "; i++)");
        appendWriteStatement(writeExternalMsgBody, arrayField.asType(), simpleArrayFieldName + "[i]");
        writeExternalMsgBody.endControlFlow();
    }

    public void appendReadArraysWithSameSizeField(MethodSpec.Builder readExternalMsgBody, String sizeFieldName, List<VariableElement> arrayFields) {
        final String fqSizeFieldName = TARGET + "." + sizeFieldName;

        readExternalMsgBody.beginControlFlow("for (" + "int i=0; i <  " + fqSizeFieldName + "; i++)");

        for (VariableElement currField : arrayFields) {
            final String simpleArrayFieldName = currField.getSimpleName().toString();
            final TypeMirror arrayComponentType = ((ArrayType) currField.asType()).getComponentType();
            appendReadStatement(readExternalMsgBody, arrayComponentType, simpleArrayFieldName + "[i]");
        }

        readExternalMsgBody.endControlFlow();
    }

    public void appendReadArrayFields(MethodSpec.Builder readExternalMethod, List<VariableElement> arrayFields) {
        for (VariableElement currArray : arrayFields) {
            appendReadArrayField(readExternalMethod, currArray);
        }
    }

    private void appendReadArrayField(MethodSpec.Builder readExternalMethod, VariableElement arrayField) {
        Validate.isTrue(arrayField.asType().getKind() == TypeKind.ARRAY);
        final String simpleArrayFieldName = arrayField.getSimpleName().toString();
        final String fqFieldArrayLength = simpleArrayFieldName + "Length";
        final TypeMirror arrayComponentType = ((ArrayType) arrayField.asType()).getComponentType();

        readExternalMethod.addStatement("int " + fqFieldArrayLength + " = " + IN + ".readInt()");
        readExternalMethod.beginControlFlow("for (" + "int i =0; i < " + fqFieldArrayLength + "; i++)");
        appendReadStatement(readExternalMethod, arrayComponentType, (simpleArrayFieldName + "[i]"));
        readExternalMethod.endControlFlow();
    }

    private boolean classTypeIsAnnotatatedAsEfficient(TypeMirror type) {
        for (Element currElement : roundEnv.getElementsAnnotatedWith(GenEfficient.class)) {
            if (currElement.asType().equals(type)) {
                return true;
            }
        }
        return false;
    }

    private String createReadEffectiveField(String effectivClassName, String fieldName) {
        return effectivClassName + ".readExternal(" + fieldName + ", " + IN + ")";
    }

    private String createWriteEffectiveField(String effectivClassName, String fieldName) {
        return effectivClassName + ".writeExternal(" + fieldName + ", " + OUT + ")";
    }

    private String toEfficientClassName(String className) {
        final int lastDotIndex = className.lastIndexOf('.');
        if (lastDotIndex > 0) {
            String simpleClassName = className.substring(lastDotIndex + 1, className.length());
            return className.replace(simpleClassName, "Efficient" + simpleClassName);
        }
        return "Efficient" + className;
    }

    public void appendInternalizeStatement(MethodSpec.Builder internalizeMethod, TypeMirror typeMirror, String fieldName) {
        TypeKind typeKind = typeMirror.getKind();
        if (TypeKind.ERROR.equals(typeKind)) {
            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR,
                    "error trying to translate field for write statement '" + fieldName + "'");
            return;
        }
        String fqFieldName = SRC + "." + fieldName;
        if (classTypeIsAnnotatatedAsEfficient(typeMirror)) {
            internalizeMethod.addStatement(createInternalizeEffectiveField(toEfficientClassName(typeMirror.toString()), fieldName));
        } else if (TypeKind.LONG.equals(typeKind) || isAssignable(typeMirror, Long.class)
                || TypeKind.DOUBLE.equals(typeKind) || isAssignable(typeMirror, Double.class)
                || TypeKind.FLOAT.equals(typeKind) || isAssignable(typeMirror, Float.class)
                || TypeKind.BOOLEAN.equals(typeKind) || isAssignable(typeMirror, Boolean.class)
                || TypeKind.INT.equals(typeKind) || isAssignable(typeMirror, Integer.class)
                || TypeKind.SHORT.equals(typeKind) || isAssignable(typeMirror, Short.class)
                || TypeKind.CHAR.equals(typeKind) || isAssignable(typeMirror, Character.class)) {
            internalizeMethod.addStatement(TARGET + "." + fieldName + " = " + SRC + "." + fieldName);
        } else if (implementsInterface(typeMirror, Externalizable.class) && hasPublicNoArgConstructor(typeMirror)) {
            internalizeMethod.addStatement(TARGET + "." + fieldName + " = " + EFFICIENT_PACKAGE + ".EfficientUtils.cloneViaExternalizable("
                    + SRC + "." + fieldName + ")");

        } else if (implementsInterface(typeMirror, Serializable.class)) {
            internalizeMethod.addStatement(TARGET + "." + fieldName + " = " + EFFICIENT_PACKAGE + ".EfficientUtils.cloneViaSerialization(" + SRC + "." + fieldName + ")");
        } else {
            inError = true;
            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR,
                    "cannot determine how to externalize field '" + fieldName + "' of type '" + typeMirror.toString() + "'" +
                            " either mark class as @GenEfficient or make it implement Serializable");
        }
    }

    public boolean hasPublicNoArgConstructor(TypeMirror type) {
        final TypeElement typeElement = processingEnv.getElementUtils().getTypeElement(type.toString());
        // Check if an empty public constructor is given
        for (Element enclosed : typeElement.getEnclosedElements()) {
            if (enclosed.getKind() == ElementKind.CONSTRUCTOR) {
                ExecutableElement constructorElement = (ExecutableElement) enclosed;
                if (constructorElement.getParameters().size() == 0 && constructorElement.getModifiers()
                        .contains(Modifier.PUBLIC)) {
                    // Found an empty constructor
                    return true;
                }
            }
        }
        return false;
    }

    private String createInternalizeEffectiveField(String effectivClassName, String fieldName) {
        return effectivClassName + ".internalize(" + SRC + "." + fieldName + "," + TARGET + "." + fieldName + ")";
    }

}
