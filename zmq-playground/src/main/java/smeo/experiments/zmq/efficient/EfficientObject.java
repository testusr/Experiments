package smeo.experiments.zmq.efficient;

import java.io.Externalizable;

/**
 * Efficient objects have implemented custom serialization (Externalizable) and can be used in gc-less fashion
 * (preallocate + internalize).
 */
public interface EfficientObject<T> extends Internalizable<T>, Externalizable {
}
