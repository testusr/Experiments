package smeo.experiments.test.utils;

import com.google.common.primitives.Primitives;
import sun.misc.Unsafe;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Helper to generate object tree and fill it with random values
 */
public class RandomClassInstanceGenerator<T> {
    private final int target_collection_size = 5;
    private Class<T> clazz;
    private static Random random = new Random();


    <T> T generateClassInstance(Class<T> clazz) {
        List<Field> allFields = getAllFields(clazz);
        List<Field> collectionFields = filterCollections(allFields);
        List<Field> valueFields = filterValueFields(allFields);
        List<Field> objectReferences = filterObjectReferences(allFields);

        T instance = newInstance(clazz);

        fillValueFields(valueFields, instance);
        return instance;
    }

    private <T> void fillObjectReferences(List<Field> objectReferences, T instance) {

    }

    private <T> void fillCollectionFields(List<Field> collectionFields, T instance) {

    }

    private <T> void fillValueFields(List<Field> valueFields, T instance) {
        Iterator<Field> iterator = valueFields.iterator();
        while (iterator.hasNext()) {
            try {

                Field currField = iterator.next();
                Class<?> type = currField.getType();
                if (type.isPrimitive()) {
                    type = Primitives.wrap(type);
                }
                if (Number.class.isAssignableFrom(type)) {
                    currField.set(instance, random.nextInt());
                } else if (Character.class.isAssignableFrom(type)){
                    currField.set(instance, 'A');
                } else if (Byte.class.isAssignableFrom(type)){
                    currField.set(instance, (byte)random.nextInt(Byte.MAX_VALUE));
                } else if (Short.class.isAssignableFrom(type)){
                    currField.set(instance, (short)random.nextInt(Short.MAX_VALUE));
                } else if (String.class.isAssignableFrom(type)) {
                    currField.set(instance, UUID.randomUUID().toString());
                }

            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * Create a new instance of a class
     *
     * @param clazz
     * @param <T>
     * @return
     */
    private <T> T newInstance(final Class<T> clazz) {
        T instance = null;
        try {
            instance = clazz.newInstance();
        } catch (InstantiationException e) {
        } catch (IllegalAccessException e) {
        }
        if (instance == null) {
            Constructor<T> constructor;
            try {
                constructor = clazz.getDeclaredConstructor();
                constructor.setAccessible(true);
                return (T) constructor.newInstance();

            } catch (NoSuchMethodException e) {
            } catch (IllegalAccessException e) {
            } catch (InstantiationException e) {
            } catch (InvocationTargetException e) {
            }

        }
        if (instance == null) {
            Unsafe unsafe = getUnsafe();
            try {
                return (T) unsafe.allocateInstance(clazz);
            } catch (InstantiationException e) {

            }

        }
        return null;
    }

    private List<Field> filterObjectReferences(List<Field> allFields) {
        return null;

    }

    private List<Field> filterValueFields(List<Field> allFields) {

        return null;
    }

    /**
     * returns all fields derived from collections and removes them from the allFields list
     *
     * @param allFields
     * @return
     */
    private List<Field> filterCollections(List<Field> allFields) {
        List<Field> collections = new ArrayList<Field>();
        Iterator<Field> iterator = allFields.iterator();
        while (iterator.hasNext()) {
            Field currField = iterator.next();
            if (Collections.class.isAssignableFrom(currField.getType())) {
                collections.add(currField);
                iterator.remove();
            }
        }
        return collections;
    }

    /**
     * @param clazz
     * @param <T>
     * @return all public and private fields trough all class hierarchies
     */
    private <T> List<Field> getAllFields(Class<T> clazz) {
        List<Field> allFields = new ArrayList<Field>();
        Class currClass = clazz;

        do {
            Field[] declaredFields = currClass.getDeclaredFields();
            for (int i = 0; i < declaredFields.length; i++) {
                declaredFields[i].setAccessible(true);
                allFields.add(declaredFields[i]);
                currClass = currClass.getSuperclass();
            }
        } while (currClass != Object.class);

        return allFields;
    }

    private static Unsafe getUnsafe() {
        try {

            Field singleoneInstanceField = Unsafe.class.getDeclaredField("theUnsafe");
            singleoneInstanceField.setAccessible(true);
            return (Unsafe) singleoneInstanceField.get(null);

        } catch (IllegalArgumentException e) {
        } catch (SecurityException e) {
        } catch (NoSuchFieldException e) {
        } catch (IllegalAccessException e) {
        }
        return null;
    }
}
