package smeo.experiments.codegen.apt;

/**
 * https://deors.wordpress.com/2011/09/26/annotation-types/
 */
public @interface Complexity {
    ComplexityLevel value() default ComplexityLevel.MEDIUM;

    enum ComplexityLevel {
        VERY_SIMPLE, SIMPLE, MEDIUM, COMPLEX, VERY_COMPLEX;
    }
}

