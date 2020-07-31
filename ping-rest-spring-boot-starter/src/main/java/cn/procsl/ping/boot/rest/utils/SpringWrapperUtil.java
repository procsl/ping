package cn.procsl.ping.boot.rest.utils;

import org.springframework.lang.Nullable;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

/**
 * @author procsl
 * @date 2020/02/19
 */
public class SpringWrapperUtil {

    static Method servletInvocableHandlerMethod;

    static Method requestResponseBodyAdviceChain;

    static Class clazz;

    public final static String clazzName = "org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyAdviceChain";

    static {
        servletInvocableHandlerMethod = ReflectionUtils.findMethod(ServletInvocableHandlerMethod.class, "wrapConcurrentResult", Object.class);
        servletInvocableHandlerMethod.setAccessible(true);

        try {
            clazz = SpringWrapperUtil.class.getClassLoader().loadClass(clazzName);

            requestResponseBodyAdviceChain = ReflectionUtils.findMethod(clazz, "getAdviceByType", List.class, Class.class);
            requestResponseBodyAdviceChain.setAccessible(true);

        } catch (ClassNotFoundException e) {
        }
    }

    public static ServletInvocableHandlerMethod callWrapConcurrentResult(ServletInvocableHandlerMethod instance, Object result) {
        Object obj = ReflectionUtils.invokeMethod(servletInvocableHandlerMethod, instance, result);
        return (ServletInvocableHandlerMethod) obj;
    }

    public static <T> List<T> callAdviceByType(@Nullable List<Object> advice, Class<T> adviceType) {
        try {
            Object tmp = requestResponseBodyAdviceChain.invoke(null, advice, adviceType);
            if (tmp != null) {
                return (List<T>) tmp;
            }
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        }
        return Collections.emptyList();
    }


}
