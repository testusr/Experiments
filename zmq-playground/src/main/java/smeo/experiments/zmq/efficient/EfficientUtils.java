package smeo.experiments.zmq.efficient;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.CachingMapper;

import java.io.*;

/**
 * Created by smeo on 20.12.16.
 */
public class EfficientUtils {

    /**
     * Copy the content of source to the target object using the {@link Externalizable} interface
     *
     * @param source
     * @param target
     */
    public static void internalizeViaExternalizable(Externalizable source, Externalizable target) {
        try {
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            final ObjectOutputStream objectOutputStream = new ObjectOutputStream(out);
            source.writeExternal(objectOutputStream);
            objectOutputStream.close();

            final ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
            final ObjectInputStream objectInputStream = new ObjectInputStream(in);
            target.readExternal(objectInputStream);
            objectInputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    public static <T extends Externalizable> T cloneViaExternalizable(T toClone) {
        return cloneViaExternalizable(toClone, (Class<T>) toClone.getClass());
    }

    public static <T extends Externalizable> T cloneViaExternalizable(T toClone, Class<T> type) {
        try {
            final T newInstance = type.newInstance();
            internalizeViaExternalizable(toClone, newInstance);
            return newInstance;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static <T extends Serializable> T cloneViaSerialization(T src) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(src);

            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            return (T) ois.readObject();
        } catch (IOException e) {
            return null;
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * Compares objects by their generated xmls. Generic way to compare objects,
     * ignoring transient attributes.
     *
     * @param objA
     * @param objB
     * @return
     */
    public static boolean equalsViaXml(Object objA, Object objB) {
        XStream xStream = xstreamForExternalizable();
        final String xmlA = xStream.toXML(objA);
        final String xmlB = xStream.toXML(objB);
        return xmlA.compareTo(xmlB) == 0;
    }

    /**
     * Created Xstream instance with enforced use of relection.
     * XStream will use the externalizable implementation by default which we might not want
     * as the output will not have attribute names and also only shows attributest that are serialized
     * trough writeExternal.
     *
     * @return
     */
    public static XStream xstreamForExternalizable() {
        XStream xstream = new XStream();
        ReflectionConverter reflectionConverter = new ReflectionConverter(
                new CachingMapper(xstream.getMapper()), xstream.getReflectionProvider()) {
            @Override
            public boolean canConvert(Class type) {
                return super.canConvert(type);
            }

            @Override
            protected void doMarshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
                super.doMarshal(source, writer, context);
            }
        };
        xstream.registerConverter(reflectionConverter, XStream.PRIORITY_LOW);
        return xstream;
    }

    /**
     * @param externalizables must not contain null values
     * @param out
     * @throws IOException
     */
    public static void writeExternalizableArray(Externalizable[] externalizables, ObjectOutput out) throws IOException {
        out.writeInt(externalizables.length);
        for (int i = 0; i < externalizables.length; i++) {
            externalizables[i].writeExternal(out);
        }
    }


    /**
     * @param in
     * @param instanceCreator
     * @param <T>
     * @return
     */
    public static <T extends EfficientObject> T[] readNewExternalizableArray(ObjectInput in, ExternalizableInstanceCreator<T> instanceCreator) throws IOException {
        final T[] array = preallocateArray(in.readInt(), instanceCreator);
        for (int i = 0; i < array.length; i++) {
            array[i].internalize(in);
        }
        return (T[]) array;
    }


    /**
     * Create an array and fill it with preallocated instances instanciated via passed creator
     *
     * @param noOfPreallocatedElemts
     * @param instanceCreator
     * @param <T>
     * @return
     */
    public static <T extends Externalizable> T[] preallocateArray(int noOfPreallocatedElemts, ExternalizableInstanceCreator<T> instanceCreator) {
        Externalizable[] array = new Externalizable[noOfPreallocatedElemts];
        for (int i = 0; i < array.length; i++) {
            array[i] = instanceCreator.newInstance();
        }
        return (T[]) array;
    }

    /**
     * Create a new array of target size, reuse the instances of the existing array and create new instances for
     * the new
     *
     * @param src
     * @param targetSize
     * @param <T>
     * @return
     */
    public static <T extends Externalizable> T[] resizeArray(T[] src, int targetSize, ExternalizableInstanceCreator<T> instanceCreator) {
        Externalizable[] arrayIntTargetSize = new Externalizable[targetSize];
        for (int i = 0; i < arrayIntTargetSize.length; i++) {
            if (i < src.length) {
                arrayIntTargetSize[i] = src[i];
            } else {
                arrayIntTargetSize[i] = instanceCreator.newInstance();
            }
        }
        return (T[]) arrayIntTargetSize;
    }


    public static void writeArray(long[] incomingStreamsLastUpdateTs, ObjectOutput out) {
    }

    public static void writeArray(double[] incomingStreamsLastUpdateTs, ObjectOutput out) {
    }


}
