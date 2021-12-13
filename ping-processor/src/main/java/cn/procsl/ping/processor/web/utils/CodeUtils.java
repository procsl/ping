package cn.procsl.ping.processor.web.utils;


import com.squareup.javapoet.TypeName;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CodeUtils {


    final static Set<String> SIMPLE_TYPES = Stream.of(
        String.class, Character.class, BigDecimal.class, BigInteger.class,
        Date.class, java.sql.Date.class, java.sql.Time.class, java.sql.Timestamp.class
    ).map(Class::getName).collect(Collectors.toSet());

    public static String convertToTemplate(Object[] args, String item) {
        if (args == null || args.length == 0) {
            return "";
        }
        return Arrays.stream(args).map(i -> item).collect(Collectors.joining(","));
    }

    public static String convertToTemplate(List<?> args, String item) {
        if (args == null || args.isEmpty()) {
            return "";
        }

        Object[] tmp = args.toArray();
        return convertToTemplate(tmp, item);
    }

    public static String convertToTemplate(int times, String item) {
        if (times <= 0) {
            return "";
        }

        Object[] tmp = new Object[times];
        return convertToTemplate(tmp, item);
    }


    public static boolean existsAny(ExecutableElement item, Class<? extends Annotation>... annotations) {
        if (annotations.length == 0) {
            return true;
        }

        for (Class<? extends Annotation> annotation : annotations) {
            if (item.getAnnotation(annotation) != null) {
                return true;
            }
        }

        return false;
    }

    public static boolean hasNeedWrapper(TypeMirror returnType) {
        String type = returnType.toString();

        if (SIMPLE_TYPES.contains(type)) {
            return true;
        }

        TypeName typeName = TypeName.get(returnType);

        if (typeName.isPrimitive()) {
            return true;
        }
        try {
            return typeName.unbox().isPrimitive();
        } catch (Exception e) {
            return false;
        }
    }

}
