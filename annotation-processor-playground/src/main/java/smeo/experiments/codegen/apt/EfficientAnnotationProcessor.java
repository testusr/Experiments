package smeo.experiments.codegen.apt;

import com.squareup.javapoet.*;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Naiv Example implementation to generate efficient
 * <p>
 * // http://hauchee.blogspot.de/2015/12/compile-time-annotation-processing-getting-class-value.html
 * // http://www.programcreek.com/java-api-examples/index.php?api=javax.lang.model.type.TypeMirror
 * http://hannesdorfmann.com/annotation-processing/annotationprocessing101
 *
 * @todo: support for several arrays with a single size field
 * @todo: size field referencing several arrays instead vice versa
 * @todo: constructor generation (no null fields -> preallocated)
 * @todo: constructor generation, efficient class needs same constructors as non efficient if to be used as replacements
 * @todo: replace unefficient versions with efficient
 * @todo: replace immutable objects (BigDecimal, String) with drop in replacements.
 */
@SupportedAnnotationTypes("smeo.experiments.codegen.apt.GenEfficient")
public class EfficientAnnotationProcessor extends AbstractProcessor {
    public static final String SRC = "src";
    public static final String TARGET = "target";
    public static final String IN = "in";
    public static final String OUT = "out";
    boolean inError = false;
    int round = -1;
    private EfficientMethodUtils methodUtils;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        inError = false;
        round++;
        methodUtils = new EfficientMethodUtils(processingEnv, roundEnv);
        final List<EfficientClassMetaData> metaData = createMetaData(annotations, roundEnv);
        final List<Class> generatedClasses = new ArrayList<>();
        EfficientClassMetaData resolvedClass = findAndRemoveCompletelyResolvedClass(metaData);

        while (resolvedClass != null) {
            Class generatedClass = generateClass(resolvedClass);
            if (generatedClass != null) {
                generatedClasses.add(generatedClass);
            }
            resolvedClass = findAndRemoveCompletelyResolvedClass(metaData);
        }


        return true;
    }

    private Class generateClass(EfficientClassMetaData resolvedClass) {

        TypeElement classElement = resolvedClass.annotatedClass();
        PackageElement packageElement = (PackageElement) classElement.getEnclosingElement();
        String className = classElement.getSimpleName().toString();
        String packageName = packageElement.getQualifiedName().toString();

        processingEnv.getMessager().printMessage(
                Diagnostic.Kind.NOTE,
                "processing annotated class: " + classElement.getQualifiedName(), classElement);

        final ClassName unefficientFqClassName = ClassName.get(packageName, className);


        final MethodSpec.Builder staticReadExternalMethodBuilder = createStaticReadExternalMethod(resolvedClass, unefficientFqClassName);
        final MethodSpec.Builder staticWriteExternalMethodBuilder = createStaticWriteExternalMethod(resolvedClass, unefficientFqClassName);
        final MethodSpec.Builder staticInternalizeMethodBuilder = createStaticInternalizeMethod(resolvedClass, unefficientFqClassName);

        resolvedClass.addToExternalize(staticWriteExternalMethodBuilder, staticReadExternalMethodBuilder);
        resolvedClass.addToInternalize(staticInternalizeMethodBuilder);

        MethodSpec staticReadExternalMethod = staticReadExternalMethodBuilder.build();
        MethodSpec staticWriteExternalMethod = staticWriteExternalMethodBuilder.build();
        MethodSpec staticInternalizeMethod = staticInternalizeMethodBuilder.build();

        final MethodSpec readExternalMethod = createReadExternalMethod(staticReadExternalMethod);
        final MethodSpec writeExternalMethod = createWriteExternalMethod(staticWriteExternalMethod);
        final MethodSpec internalizeMethod = createInternalizeMethod(staticInternalizeMethod, unefficientFqClassName);
        final List<MethodSpec> constructors = createConstructorMethods(resolvedClass);

        if (!inError) {


//            TypeVariableName p = TypeVariableName.get("T", unefficientFqClassName);


            final String generatedClassName = toEfficientClassName(className);
            final TypeSpec.Builder builder = TypeSpec.classBuilder(generatedClassName)
                    .addModifiers(Modifier.PUBLIC)
                    .superclass(TypeName.get(classElement.asType()))
                    .addSuperinterface(Externalizable.class)
                    .addSuperinterface(ParameterizedTypeName.get(
                            ClassName.get(Internalizable.class),
                            unefficientFqClassName))
//                           .addTypeVariable(p)
                    .addMethod(staticWriteExternalMethod)
                    .addMethod(staticReadExternalMethod)
                    .addMethod(writeExternalMethod)
                    .addMethod(readExternalMethod)
                    .addMethod(staticInternalizeMethod)
                    .addMethod(internalizeMethod);


            TypeSpec efficientVersion = builder
                    .build();


            JavaFile javaFile = JavaFile.builder(packageName, efficientVersion)
                    //.indent("   ")
                    .build();
            try {
                final Filer filer = processingEnv.getFiler();
                javaFile.writeTo(filer);

                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.NOTE,
                        "... generated efficient class " + generatedClassName, classElement);


            } catch (IOException e1) {
                e1.printStackTrace();
                inError = true;
            }
            return null;
        } else {

            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR,
                    "... error occured while processing class '" + className + " / round '" + round + "''. No efficient version generated");
        }
        return null;
    }

    private MethodSpec.Builder createStaticReadExternalMethod(EfficientClassMetaData resolvedClass, ClassName unefficientFqClassName) {
        return MethodSpec.methodBuilder("readExternal")
                .addParameter(unefficientFqClassName, TARGET, Modifier.FINAL)
                .addParameter(ObjectInput.class, IN, Modifier.FINAL)
                .addException(IOException.class)
                .addException(ClassNotFoundException.class)
                .addModifiers(Modifier.PUBLIC)
                .addModifiers(Modifier.STATIC)
                .returns(void.class);
    }

    private MethodSpec.Builder createStaticInternalizeMethod(EfficientClassMetaData resolvedClass, ClassName unefficientFqClassName) {
        return MethodSpec.methodBuilder("internalize")
                .addParameter(unefficientFqClassName, SRC, Modifier.FINAL)
                .addParameter(unefficientFqClassName, TARGET, Modifier.FINAL)
                .addModifiers(Modifier.PUBLIC)
                .addModifiers(Modifier.STATIC)
                .returns(void.class);
    }

    private MethodSpec.Builder createStaticWriteExternalMethod(EfficientClassMetaData resolvedClass, ClassName unefficientFqClassName) {
        return MethodSpec.methodBuilder("writeExternal")
                .addParameter(unefficientFqClassName, SRC, Modifier.FINAL)
                .addParameter(ObjectOutput.class, OUT, Modifier.FINAL)
                .addException(IOException.class)
                .addModifiers(Modifier.PUBLIC)
                .addModifiers(Modifier.STATIC)
                .returns(void.class);
    }

    private List<MethodSpec> createConstructorMethods(EfficientClassMetaData resolvedClass) {
        return null;
    }

    private MethodSpec createInternalizeMethod(MethodSpec staticInternalizeMethod, ClassName unefficientFqClassName) {
        return MethodSpec.methodBuilder("internalize")
                .addParameter(unefficientFqClassName, SRC, Modifier.FINAL)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addStatement("internalize(" + SRC + ", this)")
                .returns(void.class)
                .build();
    }

    private MethodSpec createWriteExternalMethod(MethodSpec writeExternalMethod) {
        return MethodSpec.methodBuilder("writeExternal")
                .addParameter(ObjectOutput.class, OUT, Modifier.FINAL)
                .addException(IOException.class)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addStatement("writeExternal(this, " + OUT + ")")
                .returns(void.class)
                .build();
    }

    private MethodSpec createReadExternalMethod(MethodSpec staticReadExternalMethod) {
        return MethodSpec.methodBuilder("readExternal")
                .addParameter(ObjectInput.class, IN, Modifier.FINAL)
                .addException(IOException.class)
                .addException(ClassNotFoundException.class)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addStatement("readExternal(this, " + IN + ")")
                .returns(void.class)
                .build();
    }

    private EfficientClassMetaData findAndRemoveCompletelyResolvedClass(List<EfficientClassMetaData> classMetaDatas) {
        for (EfficientClassMetaData currClassMd : classMetaDatas) {
            if (currClassMd.isResolved()) {
                classMetaDatas.remove(currClassMd);
                return currClassMd;
            }
        }
        return null;
    }


    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    private String toEfficientClassName(String className) {
        final int lastDotIndex = className.lastIndexOf('.');
        if (lastDotIndex > 0) {
            String simpleClassName = className.substring(lastDotIndex + 1, className.length());
            return className.replace(simpleClassName, "Efficient" + simpleClassName);
        }
        return "Efficient" + className;
    }

    public List<EfficientClassMetaData> createMetaData(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        List<EfficientClassMetaData> metaData = new ArrayList<>();
        final Set<? extends Element> elementsAnnotatedWithEfficient = roundEnv.getElementsAnnotatedWith(GenEfficient.class);

        for (Element e : elementsAnnotatedWithEfficient) {
            switch (e.getKind()) {
                case CLASS:
                    metaData.add(createClassMetaData((TypeElement) e));
                    break;
                default:
                    processingEnv.getMessager().printMessage(
                            Diagnostic.Kind.NOTE,
                            "cannot process annotated type: " + e.getSimpleName(), e);
                    break;
            }
        }

        return metaData;
    }

    private boolean isArrayType(VariableElement field) {
        return field.asType().getKind().equals(TypeKind.ARRAY);
    }

    private EfficientClassMetaData createClassMetaData(TypeElement annotatedClass) {
        List<EfficientFieldMetaData> efficientFields = new ArrayList<>();
        List<VariableElement> regularFields = new ArrayList<>();
        List<EfficientArrayMetaData> efficientArrays = new ArrayList<>();
        List<ExecutableElement> contructors = new ArrayList<>();

        for (Element e : annotatedClass.getEnclosedElements()) {
            if (!isTransientField(e)) {
                switch (e.getKind()) {

                    case FIELD: {
                        VariableElement field = (VariableElement) e;
                        if (isArrayType(field)) {
                            final EfficientArrayMetaData eArrayMetdata = new EfficientArrayMetaData(field);
                            efficientArrays.add(eArrayMetdata);
                            if (isAnnotatedWith(field, EfficientArray.class)) {
                                final String fieldNameRepresentingArraySize = methodUtils.getFieldReflectingCurrentArraySize(e);
                                if (fieldNameRepresentingArraySize != null) {
                                    eArrayMetdata.setFieldNameRepresentingArraySize(fieldNameRepresentingArraySize);
                                }
                            }
                        } else if (isFieldTypeAnnotatedWith(field, GenEfficient.class)) {
                            efficientFields.add(new EfficientFieldMetaData(field));
                        } else {
                            regularFields.add((VariableElement) e);
                        }
                    }
                    break;
                    case CONSTRUCTOR:
                        contructors.add((ExecutableElement) e);
                        break;
                    default: {
                        processingEnv.getMessager().printMessage(
                                Diagnostic.Kind.NOTE,
                                "cannot process annotated type in class '" + annotatedClass.getSimpleName() + "': " + e.getSimpleName(), e);
                        break;
                    }

                }
            }
        }


        groupArraysWithSameSizeField(regularFields, efficientArrays);

        return new EfficientClassMetaData(annotatedClass,
                contructors, efficientFields, regularFields, efficientArrays);
    }

    private boolean isTransientField(Element e) {
        return e.getModifiers().contains(Modifier.TRANSIENT);
    }

    private boolean isFieldTypeAnnotatedWith(VariableElement field, Class annotationClass) {
        return isAssignable(field.asType(), annotationClass);
    }

    private void groupArraysWithSameSizeField(List<VariableElement> regularFields, List<EfficientArrayMetaData> efficientArrays) {
        final Map<String, List<EfficientArrayMetaData>> fieldNameToArrays = efficientArrays.stream().collect(Collectors.groupingBy(
                EfficientArrayMetaData::getFieldNameRepresentingArraySize));
        for (Map.Entry<String, List<EfficientArrayMetaData>> currEntry : fieldNameToArrays.entrySet()) {
            if (currEntry.getKey() != EfficientArrayMetaData.NO_FIELD) {
                String sizeFieldName = currEntry.getKey();
                VariableElement sizeField = getFieldWithName(regularFields, sizeFieldName);
                if (sizeField != null) {
                    List<EfficientArrayMetaData> fieldsToCombine = currEntry.getValue();
                    EfficientArrayMetaData groupedArrayFields = new EfficientArrayMetaData(sizeField, fieldsToCombine);
                    regularFields.remove(sizeField);
                    efficientArrays.removeAll(fieldsToCombine);
                    efficientArrays.add(groupedArrayFields);
                } else {
                    processingEnv.getMessager().printMessage(
                            Diagnostic.Kind.ERROR,
                            "cannot group efficient arrays as specified size field '" + sizeFieldName + "' does not exists");
                }
            }
        }
    }

    private static VariableElement getFieldWithName(List<VariableElement> regularFields, String sizeFieldName) {
        for (VariableElement currField : regularFields) {
            final Name simpleName = currField.getSimpleName();
            if (simpleName.contentEquals(sizeFieldName)) {
                return currField;
            }
        }
        return null;
    }


    private boolean isAnnotatedWith(Element field, Class annotationClass) {
        for (AnnotationMirror currAnnotation : field.getAnnotationMirrors()) {
            if (isAssignable(currAnnotation.getAnnotationType(), annotationClass)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAssignable(TypeMirror typeMirror, Class<?> interfce) {
        final TypeMirror interfaceTypeMirror = processingEnv.getElementUtils()
                .getTypeElement(interfce.getName()).asType();

        return processingEnv.getTypeUtils().isAssignable(typeMirror, interfaceTypeMirror);
    }

    private interface EfficientMetadata {
        void addToExternalize(MethodSpec.Builder writeExternalMethod, MethodSpec.Builder readExternalMethod);

        void addToInternalize(MethodSpec.Builder internalizeMethod);
    }

    private final class EfficientClassMetaData implements EfficientMetadata {
        private final List<ExecutableElement> constructors;
        private final List<VariableElement> regularFields;
        private final List<EfficientArrayMetaData> efficientArrays;

        private TypeElement annotatedClass;
        private List<EfficientFieldMetaData> efficientFields = new ArrayList<>();


        public EfficientClassMetaData(TypeElement annotatedClass, List<ExecutableElement> constructors, List<EfficientFieldMetaData> efficientFields, List<VariableElement> regularFields, List<EfficientArrayMetaData> efficientArrays) {
            this.annotatedClass = annotatedClass;
            this.constructors = constructors;
            this.efficientFields = efficientFields;
            this.regularFields = regularFields;
            this.efficientArrays = efficientArrays;
        }

        public boolean isResolved() {
            for (EfficientFieldMetaData currMapping : efficientFields) {
                if (!currMapping.isResolved()) {
                    return false;
                }
            }
            return true;
        }

        public TypeElement annotatedClass() {
            return annotatedClass;
        }

        @Override
        public void addToExternalize(MethodSpec.Builder writeExternalMethod, MethodSpec.Builder readExternalMethod) {
            for (VariableElement currRegularField : regularFields) {
                final TypeMirror fieldTypeMirror = currRegularField.asType();
                final String simpleName = currRegularField.getSimpleName().toString();
                methodUtils.appendReadStatement(readExternalMethod, fieldTypeMirror, simpleName);
                methodUtils.appendWriteStatement(writeExternalMethod, fieldTypeMirror, simpleName);
            }
            for (EfficientFieldMetaData currEfficientField : efficientFields) {
                currEfficientField.addToExternalize(writeExternalMethod, readExternalMethod);
            }
            for (EfficientArrayMetaData currArray : efficientArrays) {
                currArray.addToExternalize(writeExternalMethod, readExternalMethod);
            }
        }

        @Override
        public void addToInternalize(MethodSpec.Builder internalizeMethod) {
            for (VariableElement currRegularField : regularFields) {
                final TypeMirror fieldTypeMirror = currRegularField.asType();
                final String simpleName = currRegularField.getSimpleName().toString();
                methodUtils.appendInternalizeStatement(internalizeMethod, fieldTypeMirror, simpleName);
            }
            for (EfficientFieldMetaData currEfficientField : efficientFields) {
                currEfficientField.addToInternalize(internalizeMethod);
            }
            for (EfficientArrayMetaData currArray : efficientArrays) {
                currArray.addToInternalize(internalizeMethod);
            }
        }

    }

    private final class EfficientFieldMetaData implements EfficientMetadata {
        private VariableElement field;
        private Class mappedEfficientClass;

        public EfficientFieldMetaData(VariableElement field) {
            this.field = field;
        }

        @Override
        public void addToExternalize(MethodSpec.Builder writeExternalMethod, MethodSpec.Builder readExternalMethod) {
            TypeMirror fieldTypeMirror = field.asType();
            final String simpleName = field.getSimpleName().toString();
            methodUtils.appendReadStatement(readExternalMethod, fieldTypeMirror, simpleName);
            methodUtils.appendWriteStatement(writeExternalMethod, fieldTypeMirror, simpleName);
        }

        @Override
        public void addToInternalize(MethodSpec.Builder internalizeMethod) {
            TypeMirror fieldTypeMirror = field.asType();
            final String simpleName = field.getSimpleName().toString();

            methodUtils.appendInternalizeStatement(internalizeMethod, fieldTypeMirror, simpleName);
        }

        public boolean isResolved() {
            return mappedEfficientClass != null;
        }
    }

    private final class EfficientArrayMetaData implements EfficientMetadata {
        public static final String NO_FIELD = ":NO_FIELD";
        private List<VariableElement> arrayFields = new ArrayList<>();
        private String fieldNameRepresentingArraySize = NO_FIELD;
        private VariableElement arraySizeField;

        public EfficientArrayMetaData(VariableElement arrayField) {
            addArrayField(arrayField);
        }

        public EfficientArrayMetaData(String sizeFieldName, List<EfficientArrayMetaData> fieldsToCombine) {
            this.fieldNameRepresentingArraySize = sizeFieldName;
            this.arraySizeField = null;
            for (EfficientArrayMetaData currArray : fieldsToCombine) {
                arrayFields.addAll(currArray.arrayFields);
            }
        }

        public EfficientArrayMetaData(VariableElement arraySizeField, List<EfficientArrayMetaData> fieldsToCombine) {
            this.arraySizeField = arraySizeField;
            this.fieldNameRepresentingArraySize = arraySizeField.getSimpleName().toString();
            for (EfficientArrayMetaData currArray : fieldsToCombine) {
                arrayFields.addAll(currArray.arrayFields);
            }
        }

        private void addArrayField(VariableElement arrayField) {
            arrayFields.add(arrayField);
        }

        @Override
        public void addToExternalize(MethodSpec.Builder writeExternalMethod, MethodSpec.Builder readExternalMethod) {
            if (arraySizeField != null) {
                final String arraySizeFieldName = arraySizeField.getSimpleName().toString();
                methodUtils.appendWriteStatement(writeExternalMethod, arraySizeField.asType(), arraySizeFieldName);
                methodUtils.appendWriteArraysWithSameSizeField(writeExternalMethod, arraySizeFieldName, arrayFields);

                methodUtils.appendReadStatement(readExternalMethod, arraySizeField.asType(), arraySizeFieldName);
                methodUtils.appendReadArraysWithSameSizeField(readExternalMethod, arraySizeFieldName, arrayFields);
            } else {
                methodUtils.appendWriteArrayFields(writeExternalMethod, arrayFields);
                methodUtils.appendReadArrayFields(readExternalMethod, arrayFields);
            }
        }

        @Override
        public void addToInternalize(MethodSpec.Builder internalizeMethod) {
            //methodUtils.appendInternalizeStatement(internalizeMethod, fieldTypeMirror, simpleName);
        }

        public void setFieldNameRepresentingArraySize(String fieldNameRepresentingArraySize) {
            this.fieldNameRepresentingArraySize = fieldNameRepresentingArraySize;
        }

        public String getFieldNameRepresentingArraySize() {
            return fieldNameRepresentingArraySize;
        }

        public void setArraySizeField(VariableElement arraySizeField) {
            this.arraySizeField = arraySizeField;
        }
    }
}
