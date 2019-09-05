package smeo.experiments.zmq.efficient;

import java.io.Externalizable;

/**
 * Created by smeo on 25.12.16.
 */
public interface ExternalizableInstanceCreator<T extends Externalizable> {
    T newInstance();
}
