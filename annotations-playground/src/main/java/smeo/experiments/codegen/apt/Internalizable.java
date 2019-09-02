package smeo.experiments.codegen.apt;

/**
 * internalize / copy all values of src object into this object.
 */
public interface Internalizable<T> {
    void internalize(T src);
}
