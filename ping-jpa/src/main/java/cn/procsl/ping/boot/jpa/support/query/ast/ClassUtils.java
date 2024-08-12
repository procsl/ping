package cn.procsl.ping.boot.jpa.support.query.ast;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

final class ClassUtils {

    /**
     * 提取给定类中的所有 get 方法和 is 方法。
     *
     * @param clazz 要提取方法的类
     * @return 包含所有 get 和 is 方法的列表
     */
    public static List<Method> extractGetAndIsMethods(Class<?> clazz) {
        List<Method> methodsList = new ArrayList<>();
        Method[] methods = clazz.getDeclaredMethods();

        for (Method method : methods) {
            if (isGetMethod(method) || isIsMethod(method)) {
                methodsList.add(method);
            }
        }

        return methodsList;
    }

    /**
     * 判断一个方法是否是 get 方法。
     *
     * @param method 要检查的方法
     * @return 如果方法是 get 方法，则返回 true；否则返回 false
     */
    public static boolean isGetMethod(Method method) {
        int modifier = method.getModifiers();
        return method.getName().startsWith("get")
            && method.getParameterCount() == 0 &&
            !Modifier.isStatic(modifier) && Modifier.isPublic(modifier)
            && !void.class.equals(method.getReturnType());
    }

    /**
     * 判断一个方法是否是 is 方法。
     *
     * @param method 要检查的方法
     * @return 如果方法是 is 方法，则返回 true；否则返回 false
     */
    public static boolean isIsMethod(Method method) {
        int modifier = method.getModifiers();
        return method.getName().startsWith("is")
            && method.getParameterCount() == 0 &&
            !Modifier.isStatic(modifier) && Modifier.isPublic(modifier)
            && (boolean.class.equals(method.getReturnType()) || Boolean.class.equals(method.getReturnType()));
    }

    /**
     * 判断一个方法是否是 set 方法。
     *
     * @param method 要检查的方法
     * @return 如果方法是 set 方法，则返回 true；否则返回 false
     */
    public static boolean isSetMethod(Method method) {
        int modifier = method.getModifiers();
        return method.getName().startsWith("set")
            && method.getParameterCount() == 1 &&
            !Modifier.isStatic(modifier) && Modifier.isPublic(modifier)
            && void.class.equals(method.getReturnType());
    }

    /**
     * 提取给定类中的所有 get, set 和 is 方法对应的字段名。
     *
     * @return 包含所有字段名的集合
     */
    public static Set<String> extractMethodNames(List<Method> methods) {
        Set<String> fieldNames = new HashSet<>();
        for (Method method : methods) {

            String name = extractMethodName(method);
            if (name != null) {
                fieldNames.add(name);
            }
        }
        return fieldNames;
    }

    public static String extractMethodName(Method method) {
        String fieldName = null;
        if (isGetMethod(method) || isIsMethod(method)) {
            fieldName = method.getName().startsWith("get")
                ? method.getName().substring(3)
                : method.getName().substring(2);
        }

        if (isSetMethod(method)) {
            fieldName = method.getName().substring(3);
        }

        if (fieldName != null && !fieldName.isEmpty()) {
            fieldName = Character.toLowerCase(fieldName.charAt(0)) + fieldName.substring(1);
        }
        return fieldName;
    }

    public static List<Field> extractFields(Class<?> clazz) {
        ArrayList<Field> ff = new ArrayList<>();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            int modifier = field.getModifiers();
            if (Modifier.isFinal(modifier)) {
                continue;
            }
            if (Modifier.isStatic(modifier)) {
                continue;
            }
            ff.add(field);
        }
        return ff;
    }

    /**
     * 判断一个类是否为常用简单类型（如 String, Date, Long, Number 等）的子类。
     *
     * @param clazz 要检查的类
     * @return 如果类为简单类型或其子类，则返回 true；否则返回 false
     */
    public static boolean isSimpleType(Class<?> clazz) {
        return clazz != null && (
            clazz == String.class ||
                clazz == Date.class ||
                clazz == Long.class ||
                clazz.isPrimitive() || // 基本数据类型
                clazz == Boolean.class ||
                clazz == Character.class ||
                clazz == Byte.class ||
                clazz == Short.class ||
                clazz == Integer.class ||
                clazz == Float.class ||
                clazz == Double.class ||
                Number.class.isAssignableFrom(clazz)
        );
    }

    /**
     * 判断一个类是否为容器类型（如 Collection、Map、数组等）。
     *
     * @param clazz 要检查的类
     * @return 如果类为容器类型，则返回 true；否则返回 false
     */
    public static boolean isContainerType(Class<?> clazz) {
        return clazz != null && (
            Collection.class.isAssignableFrom(clazz) ||
                Map.class.isAssignableFrom(clazz) ||
                clazz.isArray()
        );
    }

    public static List<Annotation> getAnnotations(AnnotatedElement... annotatedElement) {
        if (annotatedElement == null) {
            return Collections.emptyList();
        }

        ArrayList<Annotation> list = new ArrayList<>();
        for (AnnotatedElement element : annotatedElement) {
            if (element == null) {
                continue;
            }
            list.addAll(List.of(element.getAnnotations()));
        }
        return list;
    }

    @SuppressWarnings("all")
    public static <T extends Annotation> List<T> filter(List<Annotation> annotations, Class<T> clazz) {
        return annotations.stream().filter(item -> {
            return clazz.isAssignableFrom(item.getClass()) || item.getClass().isAssignableFrom(clazz);
        }).map((item) -> (T) item).toList();
    }

    @SuppressWarnings("unchecked")
    public static <T extends Annotation> T createMargeAnnotation(Class<T> clazz, List<Annotation> annotations) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, new InnerInvocationHandler(annotations));
    }

    @SuppressWarnings("unchecked")
    public static <T extends Annotation> T createMargeAnnotation(Class<T> clazz, T... element) {
        List<T> filters = filter(List.of(element), clazz);
        return filters.isEmpty() ? null : createMargeAnnotation(clazz, (List<Annotation>) filters);
    }

    private record InnerInvocationHandler(List<Annotation> annotations) implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return null;
        }

    }

}
