package smeo.experiments.zmq.efficient;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by smeo on 23.12.16.
 */
public class ReflectionUtils {
    public static List<Field> getAllFields(Object object) {
        Class<?> currClass = object.getClass();
        List<Field> allFields = new ArrayList<>();

        while (!currClass.equals(Object.class) && !currClass.getName().startsWith("java.lang")) {
            for (Field currField : currClass.getDeclaredFields()) {
                currField.setAccessible(true);
                allFields.add(currField);
            }
            currClass = currClass.getSuperclass();
        }
        return allFields;
    }


    /**
     * Verify test preparation. All non transient fields have to have a value to be verified correclty.
     * This might fail is someone has added a new attribute.
     * This is just a simple implementation and currently not deal with back references => cycles
     *
     * @param object
     */

    public static boolean verifyVerifiedNonTransientFieldsAreFilled(Object object) {
        if (object == null) return false;
        Class<?> aClass = object.getClass();
        if (aClass.isArray()) {
            int length = Array.getLength(object);
            for (int i = 0; i < length; i++) {
                Object arrayElement = Array.get(object, i);
                if (!verifyVerifiedNonTransientFieldsAreFilled(arrayElement)) {
                    return false;
                }
            }
        } else if (aClass.isAssignableFrom(Collection.class)) {
            Collection collection = (Collection) object;
            for (Object currObj : collection) {
                if (!verifyVerifiedNonTransientFieldsAreFilled(currObj)) {
                    return false;
                }
            }
        } else {
            if (object.getClass().getName().startsWith("java.lang")) {
                return true;
            }
            List<Field> allFields = ReflectionUtils.getAllFields(object);
            for (Field currField : allFields) {
                try {
                    if (!verifyVerifiedNonTransientFieldsAreFilled(currField.get(object))) {
                        return false;
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return true;
    }

}
