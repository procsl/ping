package cn.procsl.ping.processor;

import java.util.Collection;
import java.util.Collections;

public interface AnnotationAware {

    /**
     * 添加注解组件
     *
     * @param annotationComponent 注解生成组件
     * @return 如果添加失败则返回false
     */
    default boolean addAnnotationComponent(AnnotationComponent annotationComponent) {
        return false;
    }


    /**
     * 获取注解组件
     *
     * @return 获取注解组件
     */
    default Collection getAnnotationComponents() {
        return Collections.emptyList();
    }
}
