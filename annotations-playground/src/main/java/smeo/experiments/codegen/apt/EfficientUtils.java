package smeo.experiments.codegen.apt;

import java.io.*;

/**
 * Created by smeo on 15.01.17.
 */
public class EfficientUtils {

    public static <T extends Serializable> T cloneViaExternalizable(T src) {
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
