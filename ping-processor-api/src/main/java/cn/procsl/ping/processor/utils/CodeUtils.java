package cn.procsl.ping.processor.utils;


import cn.procsl.ping.processor.Expression;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.annotation.Nonnull;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CodeUtils {

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

    public static <T> Collection<T> foreach(Collection<Expression<T>> expressions, @Nonnull ProcessingEnvironment environment, @Nonnull RoundEnvironment roundEnvironment, @Nonnull Element root) {
        if (expressions == null) {
            return Collections.emptyList();
        }
        return expressions.stream().map(item -> item.interpret(environment, roundEnvironment, root)).collect(Collectors.toList());
    }

}
