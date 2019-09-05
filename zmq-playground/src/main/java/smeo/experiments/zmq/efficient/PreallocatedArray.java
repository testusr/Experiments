package smeo.experiments.zmq.efficient;

import java.io.Externalizable;

/**
 * Created by smeo on 25.12.16.
 */
public abstract class PreallocatedArray<T extends Externalizable> implements Externalizable {
    int usedElements = 0;
    Externalizable[] array;
    ExternalizableInstanceCreator<T> creator;

    PreallocatedArray(int noOfPreallocatedElemts, ExternalizableInstanceCreator<T> creator) {
        this.creator = creator;
        this.array = EfficientUtils.preallocateArray(noOfPreallocatedElemts, creator);
    }

    protected abstract T newInstance();

}
