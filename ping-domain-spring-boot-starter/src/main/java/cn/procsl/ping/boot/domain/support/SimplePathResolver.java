package cn.procsl.ping.boot.domain.support;

import com.querydsl.core.types.dsl.BeanPath;
import lombok.NonNull;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Optional;

public class SimplePathResolver implements PathResolver {

    public static final SimplePathResolver INSTANCE = new SimplePathResolver("");
    private static final String NO_CLASS_FOUND_TEMPLATE = "Did not find a query class %s for domain class %s!";
    private static final String NO_FIELD_FOUND_TEMPLATE = "Did not find a static field of the same type in %s!";
    private final String prefix;

    public SimplePathResolver(@NonNull String prefix) {
        this.prefix = prefix;
    }

    @Override
    public <T> BeanPath<T> createPath(Class<T> clazz) {

        String pathClassName = getQueryClassName(clazz);
        try {
            Class<?> pathClass = ClassUtils.forName(pathClassName, clazz.getClassLoader());

            return getStaticFieldOfType(pathClass)
                .map(it -> (BeanPath<T>) ReflectionUtils.getField(it, null))//
                .orElseThrow(() -> new IllegalStateException(String.format(NO_FIELD_FOUND_TEMPLATE, pathClass)));

        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(String.format(NO_CLASS_FOUND_TEMPLATE, pathClassName, clazz.getName()), e);
        }
    }


    private Optional<Field> getStaticFieldOfType(Class<?> type) {

        for (Field field : type.getDeclaredFields()) {

            boolean isStatic = Modifier.isStatic(field.getModifiers());
            boolean hasSameType = type.equals(field.getType());

            if (isStatic && hasSameType) {
                return Optional.of(field);
            }
        }

        Class<?> superclass = type.getSuperclass();
        return Object.class.equals(superclass) ? Optional.empty() : getStaticFieldOfType(superclass);
    }

    private String getQueryClassName(Class<?> domainClass) {

        String simpleClassName = ClassUtils.getShortName(domainClass);
        String packageName = domainClass.getPackage().getName();

        return String.format("%s%s.Q%s%s", packageName, prefix, getClassBase(simpleClassName),
            domainClass.getSimpleName());
    }

    /**
     * Analyzes the short class name and potentially returns the outer class.
     *
     * @param shortName
     * @return
     */
    private String getClassBase(String shortName) {

        String[] parts = shortName.split("\\.");

        return parts.length < 2 ? "" : parts[0] + "_";
    }

}
