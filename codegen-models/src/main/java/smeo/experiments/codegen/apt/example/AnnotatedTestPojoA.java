package smeo.experiments.codegen.apt.example;

import smeo.experiments.codegen.apt.Efficient;
import smeo.experiments.codegen.apt.EfficientArray;

/**
 * Created by smeo on 26.12.16.
 */

@Efficient
public class AnnotatedTestPojoA {
    String string;
    String[] array;
    ExternlizabelTestClass externalizable;
    Long longObject;
    long longPrimitive;
    transient long transientLong;

    int arraySizeField;
    @EfficientArray(sizeField = "arraySizeField")
    double[] doubleArrayWithSizeField;


    int secondArraySizeField;
    @EfficientArray(sizeField = "secondArraySizeField")
    int[] intArrayWithSizeField;
}
