package smeo.experiments.codegen.apt;

import java.io.*;

/**
 * Created by smeo on 15.01.17.
 */
public class EfficientUtils {

    /**
     * Class to clone needs a public no argument constructor
     *
     * @param src
     * @param <T>
     * @return
     */
    public static <T extends Externalizable> T cloneViaExternalizable(T src) {
        try {
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream outputStream = new ObjectOutputStream(out);
            src.writeExternal(outputStream);
            final T newInstance = (T) src.getClass().newInstance();
            final ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(out.toByteArray()));
            ObjectInputStream objectInputStream = new ObjectInputStream(in);
            newInstance.readExternal(objectInputStream);
            return newInstance;

        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
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
}
