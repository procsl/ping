package cn.procsl.ping.boot.rest.hook;

import org.springframework.core.Ordered;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.reflect.Method;

/**
 * @author procsl
 * @date 2020/03/10
 */
public interface RegisterMappingHook extends Ordered {


    /**
     * 路径处理钩子
     *
     * @param mapping
     * @param isClass
     * @param clazz
     * @param method
     * @param paths
     * @return
     */
    default String[] paths(RequestMapping mapping,
                           Class clazz, Method method,
                           boolean isClass,
                           String[] paths) {
        return paths;
    }

    /**
     * 方法处理钩子
     *
     * @param requestMapping
     * @param isClass
     * @param clazz
     * @param method
     * @param methods
     * @return
     */
    default RequestMethod[] methods(RequestMapping requestMapping, Class clazz, Method method, boolean isClass, RequestMethod[] methods) {
        return methods;
    }

    /**
     * 参数处理钩子
     *
     * @param requestMapping
     * @param isClass
     * @param clazz
     * @param method
     * @param params
     * @return
     */
    default String[] params(RequestMapping requestMapping, Class clazz, Method method, boolean isClass, String[] params) {
        return params;
    }

    /**
     * 请求头处理钩子
     *
     * @param requestMapping
     * @param isClass
     * @param clazz
     * @param method
     * @param headers
     * @return
     */
    default String[] headers(RequestMapping requestMapping, Class clazz, Method method, boolean isClass, String[] headers) {
        return headers;
    }

    /**
     * 接收的请求类型处理钩子
     *
     * @param requestMapping
     * @param isClass
     * @param clazz
     * @param method
     * @param consumer
     * @return
     */
    default String[] consumer(RequestMapping requestMapping, Class clazz, Method method, boolean isClass, String[] consumer) {
        return consumer;
    }

    /**
     * 生成的处理类型钩子
     *
     * @param requestMapping
     * @param isClass
     * @param clazz
     * @param method
     * @param products
     * @return
     */
    default String[] products(RequestMapping requestMapping, Class clazz, Method method, boolean isClass, String[] products) {
        return products;
    }

    /**
     * 顺序
     *
     * @return
     */
    @Override
    default int getOrder() {
        return 0;
    }
}
