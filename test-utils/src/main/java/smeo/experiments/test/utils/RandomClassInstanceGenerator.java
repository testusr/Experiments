package smeo.experiments.test.utils;

import com.google.common.primitives.Primitives;
import sun.misc.Unsafe;

import java.lang.reflect.*;
import java.util.*;

/**
 * Helper to generate object tree and fill it with random values
 */
public class RandomClassInstanceGenerator<T> {
    private final int target_collection_size = 5;
    private final int max_depth = 5;
    private Class<T> clazz;
    private static Random random = new Random();
    private int currDepth = 0;
    private static final Set<Class> primitiveWrappers = primitiveWrappers();

    <T> T generateClassInstance(Class<T> clazz) {
        currDepth++;
        T instance = null;
        try {
            if (currDepth < max_depth) {
                List<Field> allFields = getAllFields(clazz);

                instance = newInstance(clazz);

                fillValueFields(allFields, instance);
                fillObjectReferences(allFields, instance);
                fillCollectionFields(allFields, instance);
            }
        } finally {
            currDepth--;
        }

        return instance;
    }

    private <T> void fillObjectReferences(List<Field> fields, T instance) {
        List<Field> objectReferences = filterObjectReferences(fields);

        for (Field currReference : objectReferences) {
            try {
                currReference.set(instance, generateClassInstance(currReference.getType()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private <T> void fillCollectionFields(List<Field> fields, T instance) {
        List<Field> collectionFields = filterCollections(fields);
        Iterator<Field> iField = collectionFields.iterator();

        while (iField.hasNext()){
            Field currField = iField.next();
            if (List.class.isAssignableFrom(currField.getType())){
                Class genericType = getGenericType(currField);
            }
        }

    }

    private Class getGenericType(Field field) {
        Type type = field.getGenericType();

        if (type instanceof ParameterizedType) {

            ParameterizedType pType = (ParameterizedType)type;
            Type[] arr = pType.getActualTypeArguments();

            if (arr.length > 0){
                Class<?> clzz = (Class<?>) arr[0];
                return clzz;
            }
        }
        return null;

    }

    private static Set<Class> primitiveWrappers(){
        Set<Class> primitives = new HashSet<Class>();
        primitives.add(Number.class);
        primitives.add(Character.class);
        primitives.add(Byte.class);
        primitives.add(Short.class);
        primitives.add(String.class);
        primitives.add(Boolean.class);
        return primitives;
    }
    private <T> void fillValueFields(List<Field> allFields, T instance) {
        List<Field> valueFields = filterValueFields(allFields);

        Iterator<Field> iterator = valueFields.iterator();
        while (iterator.hasNext()) {
            try {

                Field currField = iterator.next();
                Class<?> type = currField.getType();
                Object value = generatePrimitiveValue(type);
                currField.set(instance, value);

            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }
    }

    private Object generatePrimitiveValue(Class<?> type) {
        Object value = null;
        if (type.isPrimitive()) {
            type = Primitives.wrap(type);
        }
        if (Long.class.isAssignableFrom(type)) {
            value =random.nextLong();
        } else if (Float.class.isAssignableFrom(type)) {
            value =random.nextFloat();
        } else if (Integer.class.isAssignableFrom(type)) {
            value =random.nextInt();
        } else if (Double.class.isAssignableFrom(type)) {
            value =random.nextDouble();
        } else if (Character.class.isAssignableFrom(type)){
            value = new Character('A');
        } else if (Byte.class.isAssignableFrom(type)){
            value =(byte)random.nextInt(Byte.MAX_VALUE);
        } else if (Short.class.isAssignableFrom(type)){
            value =(short)random.nextInt(Short.MAX_VALUE);
        } else if (String.class.isAssignableFrom(type)) {
            value = UUID.randomUUID().toString();
        } else if (Boolean.class.isAssignableFrom(type)){
            value =random.nextBoolean();
        }
        return value;
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
        return instance;
    }

    private List<Field> filterObjectReferences(List<Field> allFields) {
        Iterator<Field> iField = allFields.iterator();
        List<Field> objectReferences = new ArrayList<Field>();
        while(iField.hasNext()){
            Field currField = iField.next();
            if (!currField.getType().getName().startsWith("java.lang")){
                 if (Object.class.isAssignableFrom(currField.getType())){
                    objectReferences.add(currField);
                     iField.remove();
                 }
            }
        }

        return objectReferences;

    }

    private List<Field> filterValueFields(List<Field> allFields) {
        Iterator<Field> iField = allFields.iterator();
        List<Field> valueFields = new ArrayList<Field>();

        while (iField.hasNext()){
            Field currField = iField.next();
            if (Primitives.allPrimitiveTypes().contains(currField.getType())
                || Primitives.isWrapperType(currField.getType())){
                valueFields.add(currField);
                iField.remove();
            }
        }
        return valueFields;
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
            if (Collection.class.isAssignableFrom(currField.getType())) {
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
            }
            currClass = currClass.getSuperclass();

        } while (currClass != Object.class && currClass != null);

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
