package smeo.experiments.zmq.efficient;

/**
 * Marks objects that are able to copy the data 'internalize' from a source
 * object to its internal attributes.
 */
public interface Internalizable<T> {
    void internalize(T src);
}
