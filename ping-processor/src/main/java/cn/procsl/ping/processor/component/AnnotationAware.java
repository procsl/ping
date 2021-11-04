package cn.procsl.ping.processor.component;

import java.util.Collection;
import java.util.Collections;

public interface AnnotationAware<E> {

    /**
     * 添加注解组件
     *
     * @param annotationComponent 注解生成组件
     * @return 如果添加失败则返回false
     */
    default boolean addAnnotationComponent(AnnotationComponent<E> annotationComponent) {
        return false;
    }


    /**
     * 获取注解组件
     *
     * @return 获取注解组件
     */
    default Collection<E> getAnnotationComponents() {
        return Collections.emptyList();
    }
}
