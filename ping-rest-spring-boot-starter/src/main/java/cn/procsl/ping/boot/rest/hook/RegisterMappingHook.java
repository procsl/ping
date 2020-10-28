package cn.procsl.ping.boot.rest.hook;

import org.springframework.core.Ordered;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;

/**
 * @author procsl
 * @date 2020/03/10
 */
public interface RegisterMappingHook extends Ordered {

    /**
     * 初始化
     *
     * @param requestMappingHandlerMapping 上下文
     */
    default void init(RequestMappingHandlerMapping requestMappingHandlerMapping) {
    }

    /**
     * 开始创建时
     *
     * @param method      指定的方法
     * @param handlerType handler的类型
     */
    default void onBuild(Method method, Class<?> handlerType) {
    }

    /**
     * 处理完成时
     *
     * @param method      目标方法
     * @param handlerType 目标类型
     * @param info        映射方法
     */
    default void onComplete(Method method, Class<?> handlerType, RequestMappingInfo info) {
    }

    /**
     * 路径处理钩子
     */
    default String[] paths(RequestMapping mapping,
                           Class<?> clazz, Method method,
                           boolean isClass,
                           String[] paths) {
        return paths;
    }

    /**
     * 方法处理钩子
     */
    default RequestMethod[] methods(RequestMapping requestMapping, Class<?> clazz, Method method, boolean isClass, RequestMethod[] methods) {
        return methods;
    }

    /**
     * 参数处理钩子
     */
    default String[] params(RequestMapping requestMapping, Class<?> clazz, Method method, boolean isClass, String[] params) {
        return params;
    }

    /**
     * 请求头处理钩子
     */
    default String[] headers(RequestMapping requestMapping, Class<?> clazz, Method method, boolean isClass, String[] headers) {
        return headers;
    }

    /**
     * 接收的请求类型处理钩子
     */
    default String[] consumer(RequestMapping requestMapping, Class<?> clazz, Method method, boolean isClass, String[] consumer) {
        return consumer;
    }

    /**
     * 生成的处理类型钩子
     */
    default String[] products(RequestMapping requestMapping, Class<?> clazz, Method method, boolean isClass, String[] products) {
        return products;
    }

    /**
     * 顺序
     */
    @Override
    default int getOrder() {
        return 0;
    }
}
