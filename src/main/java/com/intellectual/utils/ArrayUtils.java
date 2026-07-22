package com.intellectual.utils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

/**
 * 数组工具类
 */
public class ArrayUtils {

    /**
     * 判断元素是否在数组中
     *
     * @param array 数组
     * @param element 要查找的元素
     * @return true-存在, false-不存在
     */
    public static boolean contains(Integer[] array, Integer element) {
        if (isEmpty(array)) {
            return false;
        }
        for (Integer item : array) {
            if (item != null && item.equals(element)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断元素是否在数组中 (long类型)
     *
     * @param array 数组
     * @param element 要查找的元素
     * @return true-存在, false-不存在
     */
    public static boolean contains(Long[] array, Long element) {
        if (isEmpty(array)) {
            return false;
        }
        for (Long item : array) {
            if (item != null && item.equals(element)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断元素是否在数组中 (String类型)
     *
     * @param array 数组
     * @param element 要查找的元素
     * @return true-存在, false-不存在
     */
    public static boolean contains(String[] array, String element) {
        if (isEmpty(array)) {
            return false;
        }
        for (String item : array) {
            if (item != null && item.equals(element)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断元素是否在数组中 (泛型方法)
     *
     * @param array 数组
     * @param element 要查找的元素
     * @return true-存在, false-不存在
     */
    public static <T> boolean contains(T[] array, T element) {
        if (isEmpty(array)) {
            return false;
        }
        for (T item : array) {
            if (item != null && item.equals(element)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断数组是否为空
     *
     * @param array 数组
     * @return true-空, false-非空
     */
    public static boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    /**
     * 判断数组是否不为空
     *
     * @param array 数组
     * @return true-非空, false-空
     */
    public static boolean isNotEmpty(Object[] array) {
        return !isEmpty(array);
    }

    /**
     * 获取数组长度
     *
     * @param array 数组
     * @return 数组长度, 如果为null返回0
     */
    public static int size(Object[] array) {
        if (array == null) {
            return 0;
        }
        return array.length;
    }

    /**
     * 合并两个数组
     *
     * @param array1 数组1
     * @param array2 数组2
     * @return 合并后的数组
     */
    public static <T> T[] merge(T[] array1, T[] array2) {
        if (isEmpty(array1)) {
            return array2;
        }
        if (isEmpty(array2)) {
            return array1;
        }
        T[] result = Arrays.copyOf(array1, array1.length + array2.length);
        System.arraycopy(array2, 0, result, array1.length, array2.length);
        return result;
    }

    /**
     * 对数组进行排序（自然顺序），原数组会被修改
     *
     * @param array 数组
     * @return 排序后的原数组
     */
    public static <T extends Comparable<? super T>> T[] sort(T[] array) {
        if (isNotEmpty(array)) {
            Arrays.sort(array);
        }
        return array;
    }

    /**
     * 对数组进行排序（自定义比较器），原数组会被修改
     *
     * @param array 数组
     * @param comparator 比较器
     * @return 排序后的原数组
     */
    public static <T> T[] sort(T[] array, Comparator<? super T> comparator) {
        if (isNotEmpty(array)) {
            Arrays.sort(array, comparator);
        }
        return array;
    }

    /**
     * 对数组进行排序，返回新数组，原数组不变
     *
     * @param array 数组
     * @return 排序后的新数组
     */
    public static <T extends Comparable<? super T>> T[] sorted(T[] array) {
        if (isEmpty(array)) {
            return array;
        }
        T[] result = array.clone();
        Arrays.sort(result);
        return result;
    }

    /**
     * 对数组进行排序（自定义比较器），返回新数组，原数组不变
     *
     * @param array 数组
     * @param comparator 比较器
     * @return 排序后的新数组
     */
    public static <T> T[] sorted(T[] array, Comparator<? super T> comparator) {
        if (isEmpty(array)) {
            return array;
        }
        T[] result = array.clone();
        Arrays.sort(result, comparator);
        return result;
    }

    /**
     * 数组转Set
     *
     * @param array 数组
     * @return Set集合
     */
    public static <T> Set<T> toSet(T[] array) {
        if (isEmpty(array)) {
            return new HashSet<>();
        }
        return new HashSet<>(Arrays.asList(array));
    }

    /**
     * 打印数组内容
     *
     * @param array 数组
     * @return 数组字符串表示
     */
    public static String toString(Object[] array) {
        if (isEmpty(array)) {
            return "[]";
        }
        return Arrays.toString(array);
    }

    /**
     * 从源对象复制相同字段到目标对象
     * 将源对象中与目标对象同名且同类型的字段值复制到目标对象
     * 只复制非null值，返回目标对象
     *
     * @param source 源对象（被复制的对象）
     * @param target 目标对象（要复制到的对象）
     * @return 复制后的目标对象
     */
    public static <T> T copyProperties(Object source, T target) {
        if (source == null || target == null) {
            return target;
        }

        Class<?> sourceClass = source.getClass();
        Class<?> targetClass = target.getClass();

        // 获取目标对象的所有字段
        Field[] targetFields = getAllFields(targetClass);

        try {
            for (Field targetField : targetFields) {
                String fieldName = targetField.getName();
                Class<?> fieldType = targetField.getType();

                // 在源对象中查找相同名称和类型的字段
                Field sourceField = findField(sourceClass, fieldName, fieldType);
                
                if (sourceField != null) {
                    // 设置可访问
                    sourceField.setAccessible(true);
                    targetField.setAccessible(true);
                    
                    // 获取源对象的字段值
                    Object value = sourceField.get(source);
                    
                    // 只复制非null值
                    if (value != null) {
                        targetField.set(target, value);
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("复制字段值失败", e);
        }

        return target;
    }

    /**
     * 从源对象复制相同字段到目标对象（包括null值）
     * 将源对象中与目标对象同名且同类型的字段值复制到目标对象
     * 即使值为null也会复制，返回目标对象
     *
     * @param source 源对象（被复制的对象）
     * @param target 目标对象（要复制到的对象）
     * @return 复制后的目标对象
     */
    public static <T> T copyPropertiesIncludeNull(Object source, T target) {
        if (source == null || target == null) {
            return target;
        }

        Class<?> sourceClass = source.getClass();
        Class<?> targetClass = target.getClass();

        Field[] targetFields = getAllFields(targetClass);

        try {
            for (Field targetField : targetFields) {
                String fieldName = targetField.getName();
                Class<?> fieldType = targetField.getType();

                Field sourceField = findField(sourceClass, fieldName, fieldType);
                
                if (sourceField != null) {
                    sourceField.setAccessible(true);
                    targetField.setAccessible(true);
                    
                    Object value = sourceField.get(source);
                    targetField.set(target, value);
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("复制字段值失败", e);
        }

        return target;
    }

    /**
     * 在类中查找指定名称和类型的字段
     *
     * @param clazz 类
     * @param fieldName 字段名
     * @param fieldType 字段类型
     * @return 找到的字段，未找到返回null
     */
    private static Field findField(Class<?> clazz, String fieldName, Class<?> fieldType) {
        while (clazz != null && !clazz.equals(Object.class)) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                if (field.getType().equals(fieldType)) {
                    return field;
                }
            } catch (NoSuchFieldException e) {
                // 继续查找父类
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    /**
     * 获取类的所有字段（包括父类的字段）
     *
     * @param clazz 类
     * @return 字段数组
     */
    private static Field[] getAllFields(Class<?> clazz) {
        java.util.List<Field> fieldList = new java.util.ArrayList<>();
        
        while (clazz != null && !clazz.equals(Object.class)) {
            Field[] declaredFields = clazz.getDeclaredFields();
            fieldList.addAll(Arrays.asList(declaredFields));
            clazz = clazz.getSuperclass();
        }
        
        return fieldList.toArray(new Field[0]);
    }
}
