package cn.procsl.ping.boot.jpa.support.query.repository;

import cn.procsl.ping.boot.jpa.support.query.From;
import cn.procsl.ping.boot.jpa.support.query.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.StringUtils;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@Slf4j
class PredicateAnnotationExtractor {

    public static List<PredicateMeta> extract(Object dto) {

        HashMap<String, List<PredicateMetaRecord>> map = extractedPredicateMeta(dto);

        // 合并
        List<PredicateMeta> result = new ArrayList<>();
        map.forEach((k, v) -> result.add(new PredicateMetaWrapper(v)));
        return result;
    }

    private record PredicateMetaWrapper(List<PredicateMetaRecord> meta) implements PredicateMeta {

        @Override
        public boolean isRoot() {
            return meta.getFirst().isRoot();
        }

        @Override
        public Predicate getPredicate() {
            List<Predicate> list = meta.stream().map(PredicateMetaRecord::getPredicate).collect(Collectors.toList());
            return new PredicateWrapper(list);
        }

        @Override
        public List<Parameter> extractParameterPlaceholderAndValue() {
            return null;
        }

        @Override
        public Operator createOperator(From from) {
            return null;
        }

        @Override
        public boolean shouldIgnore() {
            return false;
        }

        @Override
        public boolean ignoreIfBlank() {
            return false;
        }
    }

    @RequiredArgsConstructor
    static class PredicateWrapper implements Predicate {

        final List<Predicate> preedicates;

        @Override
        public String group() {
            return null;
        }

        @Override
        public boolean ignoreIfNull() {
            return false;
        }

        @Override
        public boolean ignoreIfBlank() {
            return false;
        }

        @Override
        public boolean ignoreIfEmptyCollection() {
            return false;
        }

        @Override
        public OperatorType operator() {
            return null;
        }

        @Override
        public String[] path() {
            return new String[0];
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return null;
        }
    }

    private static HashMap<String, List<PredicateMetaRecord>> extractedPredicateMeta(Object dto) {
        Class<?> clazz = dto.getClass();
        PropertyDescriptor[] props = BeanUtils.getPropertyDescriptors(clazz);
        HashMap<String, List<PredicateMetaRecord>> predicateMaps = new HashMap<>();
        BiConsumer<String, Predicate> bi = (s, predicate) -> {
            s = init(s, predicateMaps);
            ValueType value = null;
            String current = "$";
            if (!s.equals("$")) {
                value = resolve(dto, s);
            }
            PredicateMetaRecord m;
            if (value != null) {
                m = new PredicateMetaRecord(value.method().getReturnType(),
                    value.value(), s, predicate, PredicateMetaRecord.Source.type, current, value.field(), value.method());
            } else {
                m = new PredicateMetaRecord(null,
                    null, s, predicate, PredicateMetaRecord.Source.type, current, null, null);
            }
            predicateMaps.get(s).add(m);
        };
        flatToMap(clazz, null, bi);

        for (PropertyDescriptor prop : props) {
            String fieldName = prop.getName();
            if ("class".equals(fieldName)) continue;

            Field field = findField(clazz, fieldName);
            Method getter = prop.getReadMethod();
            if (getter == null) continue;

            // 尝试从字段上取注解
            Class<?> returnType = getter.getReturnType();
            Object value = readValue(dto, getter);
            if (field != null) {
                flatToMap(field, fieldName, (s, predicate) -> {
                    s = init(s, predicateMaps);
                    PredicateMetaRecord meta = new PredicateMetaRecord(returnType, value, s, predicate, PredicateMetaRecord.Source.field, fieldName, field, getter);
                    predicateMaps.get(s).add(meta);
                });
            }

            // 提取方法上的
            flatToMap(getter, fieldName, (s, predicate) -> {
                s = init(s, predicateMaps);
                predicateMaps.get(s).add(new PredicateMetaRecord(returnType, value, s, predicate, PredicateMetaRecord.Source.method, fieldName, field, getter));
            });
        }
        return predicateMaps;
    }

    private static String init(String s, HashMap<String, List<PredicateMetaRecord>> predicateMaps) {
        if (s == null) {
            s = "";
        }
        s = s.trim();
        if (!StringUtils.hasText(s)) {
            s = "$";
        }
        predicateMaps.computeIfAbsent(s, k -> new ArrayList<>());
        return s;
    }

    private static void flatToMap(AnnotatedElement annotated, String fieldName, BiConsumer<String, Predicate> push) {
        Set<Predicate> predicates = AnnotatedElementUtils.findMergedRepeatableAnnotations(annotated, Predicate.class);
        for (Predicate predicate : predicates) {
            String[] paths = predicate.path();
            if (paths == null || paths.length == 0) {
                push.accept(fieldName, predicate);
                continue;
            }

            List<String> r = Arrays.stream(paths).filter(item -> item != null && !item.isBlank()).distinct().toList();
            if (r.isEmpty()) {
                push.accept(fieldName, predicate);
                continue;
            }

            for (String path : paths) {
                push.accept(path, predicate);
            }
        }
    }

    private static Field findField(Class<?> clazz, String name) {
        try {
            return clazz.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    private static Object readValue(Object obj, Method getter) {
        try {
            return getter.invoke(obj);
        } catch (Exception e) {
            throw new RuntimeException("无法获取值: " + getter.getName(), e);
        }
    }


    /**
     * 解析路径对应的字段类型类别
     */
    public static Class<?> resolveTypeCategory(Class<?> rootClass, String path) {
        String[] segments = path.split("\\.");
        Class<?> current = rootClass;

        for (String segment : segments) {
            PropertyDescriptor pd = getPropertyDescriptor(current, segment);
            if (pd == null || pd.getReadMethod() == null) {
                throw new IllegalArgumentException("字段不存在或无法访问: " + segment + " in " + current);
            }
            current = pd.getPropertyType();
        }

        return current;
    }

    public record ValueType(Field field, Method method, Object value, String name) {
    }

    /**
     * 通过路径表达式获取对象属性的字段、方法和值，如 user.address.city
     *
     * @param bean 源对象
     * @param path 路径表达式，如 "user.address.name"
     * @return 最终字段的 ValueType 记录，包含字段、方法和值
     */
    public static ValueType resolve(Object bean, String path) {
        if (bean == null || path == null || path.isBlank()) {
            return null;
        }

        String[] segments = path.split("\\.");
        Object current = bean;
        Class<?> currentClass = bean.getClass();
        Method lastMethod = null;
        Field lastField = null;

        String lastName = null;
        for (String segment : segments) {

            PropertyDescriptor pd = getPropertyDescriptor(currentClass, segment);
            if (pd == null || pd.getReadMethod() == null) {
                throw new IllegalArgumentException("无法读取字段: " + segment + " in " + currentClass);
            }

            Method getter = pd.getReadMethod();
            // 获取实际值
            if (current != null) {
                try {
                    current = getter.invoke(current);
                } catch (Exception e) {
                    log.warn("获取值失败: {}", current);
                    current = null;
                }
            }
            currentClass = getter.getReturnType();
            lastMethod = getter;
            lastField = getDeclaredField(bean.getClass(), segment);
            lastName = segment;
        }

        return new ValueType(lastField, lastMethod, current, lastName);
    }

    private static PropertyDescriptor getPropertyDescriptor(Class<?> clazz, String propertyName) {
        try {
            for (PropertyDescriptor pd : Introspector.getBeanInfo(clazz).getPropertyDescriptors()) {
                if (pd.getName().equals(propertyName)) {
                    return pd;
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static Field getDeclaredField(Class<?> clazz, String fieldName) {
        while (clazz != null && clazz != Object.class) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }

}
